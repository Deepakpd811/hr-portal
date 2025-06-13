package com.capgemini.hrmanagement.hr_portal.controller;

import com.capgemini.hrmanagement.hr_portal.client.DepartmentClient;
import com.capgemini.hrmanagement.hr_portal.client.EmployeeClient;
import com.capgemini.hrmanagement.hr_portal.client.JobClient;
import com.capgemini.hrmanagement.hr_portal.dto.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

@Controller
@Slf4j
@RequestMapping("/salary-ui")
@RequiredArgsConstructor
public class SalaryController {

    @Autowired
    EmployeeClient employeeClient;

    @Autowired
    DepartmentClient departmentClient;

    @Autowired
    JobClient jobClient;

    @Autowired
    ObjectMapper objectMapper;

    private final WebClient webClient;

    // GET: Display employees with salary > amount
    @GetMapping
    public String getEmployeesWithHighSalary(
            @RequestParam(defaultValue = "5000") BigDecimal amount,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            Model model) {

        // Request one extra item to detect if next page exists
        int fetchSize = size + 1;

        String uri = String.format("/api/employees/salary_greater_than/%s?page=%d&size=%d", amount, page, fetchSize);

        ApiResponseDtowithoutpage<EmployeeDTO> response = webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiResponseDtowithoutpage<EmployeeDTO>>() {
                })
                .block();

        List<EmployeeDTO> employees = response != null ? response.getData() : List.of();

        // Check if there is next page
        boolean hasNext = employees.size() == fetchSize;

        // Show only 'size' items on the current page
        if (hasNext) {
            employees = employees.subList(0, size);
        }

        model.addAttribute("employees", employees);
        model.addAttribute("amount", amount);
        model.addAttribute("page", page);
        model.addAttribute("size", size);
        model.addAttribute("hasNext", hasNext);

        return "salary/employee-salary-list";
    }

    @GetMapping("/employee/edit/{id}")
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

            return "salary/edit";

        } catch (Exception e) {
            log.error("Failed to load employee form for ID {}: {}", id, e.getMessage(), e);
            model.addAttribute("errorMessage", "Error loading form: " + e.getMessage());
            return "error";
        }
    }


    @PostMapping("/employee/update")
    public String updateEmployee(@ModelAttribute("employee") EmployeeDTO employeeDTO, Model model) throws Exception {
        // Preserve original hire date
        ApiResponseDtowithoutpageSingleData response = employeeClient.getEmployeeById(employeeDTO.getEmployeeId());
        LinkedHashMap<String, Object> data = (LinkedHashMap<String, Object>) response.getData();

        employeeDTO.setHireDate(LocalDate.parse((String) data.get("hireDate")));

        employeeClient.updateEmployee(employeeDTO.getEmployeeId(), employeeDTO);

        return "redirect:/salary-ui";
    }

    @GetMapping("/employee/new")
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

        return "salary/create-employee";
    }

    // New method for saving employee
    @PostMapping("/employee/save")
    public String saveEmployee(@ModelAttribute("employee") EmployeeDTO employeeDTO, RedirectAttributes redirectAttributes) {
        try {
            employeeClient.createEmployee(employeeDTO);
            redirectAttributes.addFlashAttribute("message", "Employee added successfully!");
            return "redirect:/salary-ui";
        } catch (Exception e) {
            log.error("Error saving employee", e);
            redirectAttributes.addFlashAttribute("error", "Failed to add employee: " + e.getMessage());
            return "redirect:/salary-ui/employee/new";
        }
    }
}