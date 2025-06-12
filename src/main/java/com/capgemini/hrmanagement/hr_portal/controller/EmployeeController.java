package com.capgemini.hrmanagement.hr_portal.controller;

import com.capgemini.hrmanagement.hr_portal.client.DepartmentClient;
import com.capgemini.hrmanagement.hr_portal.client.EmployeeClient;
import com.capgemini.hrmanagement.hr_portal.client.JobClient;
import com.capgemini.hrmanagement.hr_portal.dto.ApiResponseDtowithoutpageSingleData;
import com.capgemini.hrmanagement.hr_portal.dto.DepartmentDTO;
import com.capgemini.hrmanagement.hr_portal.dto.EmployeeDTO;
import com.capgemini.hrmanagement.hr_portal.dto.JobDTO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
public class EmployeeController {

    private final EmployeeClient employeeClient;
    private final ObjectMapper objectMapper;

    public EmployeeController(EmployeeClient employeeClient, ObjectMapper objectMapper) {
        this.employeeClient = employeeClient;
        this.objectMapper = objectMapper;
    }

    @Autowired
    private DepartmentClient departmentClient; // Feign client for department operations

    @Autowired
    private JobClient jobClient; // Feign client for job operations

    @GetMapping("/employees")
    public String showEmployees(
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        ApiResponseDtowithoutpageSingleData<?> response = employeeClient.getAllEmployees(page, 10); // fetch paginated

        Map<String, Object> data = objectMapper.convertValue(
                response.getData(),
                new TypeReference<Map<String, Object>>() {}
        );

        List<EmployeeDTO> employees = objectMapper.convertValue(
                data.get("employees"),
                new TypeReference<List<EmployeeDTO>>() {}
        );

        model.addAttribute("employees", employees);
        model.addAttribute("currentPage", data.get("currentPage"));
        model.addAttribute("totalPages", data.get("totalPages"));

        return "employee-list";
    }


    @GetMapping("/employees/edit/{id}")
    public String showEditForm(@PathVariable("id") BigDecimal id, Model model) {
        try {
            ApiResponseDtowithoutpageSingleData response = employeeClient.getEmployeeById(id);
            LinkedHashMap<String, Object> data = (LinkedHashMap<String, Object>) response.getData();

            log.info("====== Raw employee data for ID {} ======", id);
            data.forEach((key, value) -> log.info("{}: {}", key, value));

            EmployeeDTO employee = new EmployeeDTO();

            // Basic Info
            employee.setEmployeeId(new BigDecimal(data.get("employeeId").toString()));
            employee.setFirstName((String) data.get("firstName"));
            employee.setLastName((String) data.get("lastName"));
            employee.setEmail((String) data.get("email"));
            employee.setPhoneNumber((String) data.get("phoneNumber"));

            // Salary
            Object salaryRaw = data.get("salary");
            if (salaryRaw != null && !salaryRaw.toString().isBlank()) {
                try {
                    BigDecimal salary = new BigDecimal(salaryRaw.toString());
                    employee.setSalary(salary);
                    log.info("Parsed Salary: {}", salary);
                } catch (Exception e) {
                    log.warn("Failed to parse salary: {}", salaryRaw, e);
                }
            } else {
                log.warn("Salary not present or empty.");
            }

            // Job ID
            if (data.get("job_Id") != null) {
                employee.setJob_Id(data.get("job_Id").toString());
                log.info("Job ID: {}", data.get("job_Id"));
            }

            // Department ID
            if (data.get("department_Id") != null) {
                try {
                    BigDecimal deptId = new BigDecimal(data.get("department_Id").toString());
                    employee.setDepartment_Id(deptId);
                    log.info("Department ID: {}", deptId);
                } catch (Exception e) {
                    log.warn("Failed to parse department_Id: {}", data.get("department_Id"), e);
                }
            }

            // Manager ID
            Object managerRaw = data.get("manager_Id");
            if (managerRaw != null && !managerRaw.toString().isBlank()) {
                try {
                    BigDecimal managerId = new BigDecimal(managerRaw.toString());
                    employee.setManager_Id(managerId);
                    log.info("Parsed Manager ID: {}", managerId);
                } catch (Exception e) {
                    log.warn("Failed to parse manager_Id: {}", managerRaw, e);
                }
            } else {
                log.warn("Manager ID not present or empty.");
            }

            // Fetch departments
            List<DepartmentDTO> departments = Collections.emptyList();
            try {
                ResponseEntity<ApiResponseDtowithoutpageSingleData> departmentResponse = departmentClient.getAllDepartments();
                if (departmentResponse.getStatusCode().is2xxSuccessful() && departmentResponse.getBody() != null) {
                    departments = objectMapper.convertValue(departmentResponse.getBody().getData(), new TypeReference<List<DepartmentDTO>>() {});
                    log.info("Fetched {} departments", departments.size());
                }
            } catch (Exception e) {
                log.error("Error fetching departments", e);
                model.addAttribute("error", "Failed to fetch departments: " + e.getMessage());
            }

            // Fetch jobs
            List<JobDTO> jobs = Collections.emptyList();
            try {
                ResponseEntity<ApiResponseDtowithoutpageSingleData> jobResponse = jobClient.findAll();
                if (jobResponse.getStatusCode().is2xxSuccessful() && jobResponse.getBody() != null) {
                    jobs = objectMapper.convertValue(jobResponse.getBody().getData(), new TypeReference<List<JobDTO>>() {});
                    log.info("Fetched {} jobs", jobs.size());
                }
            } catch (Exception e) {
                log.error("Error fetching jobs", e);
                model.addAttribute("error", "Failed to fetch jobs: " + e.getMessage());
            }

            // Add attributes
            model.addAttribute("employee", employee);
            model.addAttribute("departments", departments);
            model.addAttribute("jobs", jobs);

            return "edit";

        } catch (Exception e) {
            log.error("Failed to load employee form for ID {}: {}", id, e.getMessage(), e);
            model.addAttribute("errorMessage", "Error loading form: " + e.getMessage());
            return "error";
        }
    }


    @PostMapping("/employees/update")
    public String updateEmployee(@ModelAttribute("employee") EmployeeDTO employeeDTO, Model model) throws Exception {
        // Preserve original hire date
        ApiResponseDtowithoutpageSingleData response = employeeClient.getEmployeeById(employeeDTO.getEmployeeId());
        LinkedHashMap<String, Object> data = (LinkedHashMap<String, Object>) response.getData();

        employeeDTO.setHireDate(LocalDate.parse((String) data.get("hireDate")));

        employeeClient.updateEmployee(employeeDTO.getEmployeeId(), employeeDTO);

        return "redirect:/employees";
    }

    @GetMapping("/employees/new")
    public String showCreateForm(Model model) {
        List<DepartmentDTO> departments = Collections.emptyList();
        List<JobDTO> jobs = Collections.emptyList();

        try {
            ResponseEntity<ApiResponseDtowithoutpageSingleData> departmentResponse = departmentClient.getAllDepartments();
            if (departmentResponse.getStatusCode().is2xxSuccessful() && departmentResponse.getBody() != null) {
                Object rawData = departmentResponse.getBody().getData();
                departments = objectMapper.convertValue(rawData, new TypeReference<List<DepartmentDTO>>() {});
            } else {
                log.error("Department API call failed with status: {}", departmentResponse.getStatusCode());
                model.addAttribute("error", "Failed to fetch departments.");
            }
        } catch (Exception e) {
            log.error("Error fetching departments", e);
            model.addAttribute("error", "Failed to fetch departments: " + e.getMessage());
        }

        try {
            ResponseEntity<ApiResponseDtowithoutpageSingleData> jobResponse = jobClient.findAll();
            if (jobResponse.getStatusCode().is2xxSuccessful() && jobResponse.getBody() != null) {
                Object rawData = jobResponse.getBody().getData();
                jobs = objectMapper.convertValue(rawData, new TypeReference<List<JobDTO>>() {});
            } else {
                log.error("Job API call failed with status: {}", jobResponse.getStatusCode());
                model.addAttribute("error", "Failed to fetch jobs.");
            }
        } catch (Exception e) {
            log.error("Error fetching jobs", e);
            model.addAttribute("error", "Failed to fetch jobs: " + e.getMessage());
        }

        model.addAttribute("employee", new EmployeeDTO());
        model.addAttribute("departments", departments);
        model.addAttribute("jobs", jobs);

        return "create-employee";
    }

    @PostMapping("/employees/save")
    public String saveEmployee(@ModelAttribute("employee") EmployeeDTO employeeDTO, RedirectAttributes redirectAttributes) {
        try {
            employeeClient.createEmployee(employeeDTO);
            redirectAttributes.addFlashAttribute("message", "Employee added successfully!");
            return "redirect:/employees";
        } catch (Exception e) {
            log.error("Error saving employee", e);
            redirectAttributes.addFlashAttribute("error", "Failed to add employee: " + e.getMessage());
            return "redirect:/employees/new";
        }
    }
}