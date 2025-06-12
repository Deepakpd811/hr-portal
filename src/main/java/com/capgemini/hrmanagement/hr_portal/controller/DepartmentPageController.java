package com.capgemini.hrmanagement.hr_portal.controller;

import com.capgemini.hrmanagement.hr_portal.dto.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Controller
public class DepartmentPageController {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public DepartmentPageController(WebClient webClient) {
        this.webClient = webClient;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @GetMapping("/")
    public String homePage() {
        return "index";
    }

    @GetMapping("/employees-by-department")
    public String getEmployeesByDepartment(
            @RequestParam(name = "departmentName", defaultValue = "Sales") String selectedDepartmentName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "employeeId") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            Model model) {
        populateDepartmentsDropdown(model);
        populateEmployeesTable(selectedDepartmentName, page, size, sortBy, sortDir, model);
        populateModalDropdowns(model);
        model.addAttribute("selectedDepartmentName", selectedDepartmentName);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");
        return "employees_by_department";
    }

    @PostMapping("/department/create")
    public String createDepartment(CreateDepartmentRequestDTO newDepartmentDto, RedirectAttributes redirectAttributes) {
        try {
            webClient.post().uri("/api/department").bodyValue(newDepartmentDto).retrieve().toBodilessEntity().block();
            redirectAttributes.addFlashAttribute("successMessage", "Department '" + newDepartmentDto.getDepartmentName() + "' created successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error creating department: " + e.getMessage());
        }
        return "redirect:/employees-by-department?departmentName=" + newDepartmentDto.getDepartmentName();
    }

    @PostMapping("/employees/update-department")
    public String updateEmployeeDepartment(@RequestParam BigDecimal employeeId, @RequestParam BigDecimal departmentId, @RequestParam String fromDept, RedirectAttributes redirectAttributes) {
        UpdateEmployeeDepartmentDTO dto = new UpdateEmployeeDepartmentDTO(departmentId);
        try {
            webClient.put().uri("/api/employee-dept/" + employeeId).bodyValue(dto).retrieve().toBodilessEntity().block();
            redirectAttributes.addFlashAttribute("successMessage", "Employee department updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating department: " + e.getMessage());
        }
        return "redirect:/employees-by-department?departmentName=" + fromDept;
    }

    @GetMapping("/employees/{id}/edit-department")
    public String showEditDepartmentForm(@PathVariable BigDecimal id,
                                         @RequestParam String fromDept,
                                         Model model,
                                         RedirectAttributes redirectAttributes) {
        // 1. Fetch the employee to be edited
        Optional<ApiResponseDtowithoutpageSingleData<EmployeeDetailDTO>> employeeResponse = fetchData(() ->
                webClient.get().uri("/api/employees/" + id).retrieve().bodyToMono(new ParameterizedTypeReference<>() {}));

        if (employeeResponse.isEmpty() || employeeResponse.get().getData() == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Could not fetch employee details for ID: " + id);
            return "redirect:/employees-by-department";
        }
        model.addAttribute("employee", employeeResponse.get().getData());

        // 2. Fetch the FULL LIST of DepartmentDTOs for the dropdown
        Optional<ApiResponseDtowithoutpage<DepartmentDTO>> deptsResponse = fetchData(() ->
                webClient.get().uri("/api/department").retrieve().bodyToMono(new ParameterizedTypeReference<>() {}));

        // Add the list of DepartmentDTOs (not just names) to the model
        model.addAttribute("departments", deptsResponse.map(ApiResponseDtowithoutpage::getData).orElse(Collections.emptyList()));
        model.addAttribute("fromDept", fromDept);

        return "edit_employee_department";
    }

    private void populateDepartmentsDropdown(Model model) {
        Optional<ApiResponseDtowithoutpage<DepartmentDTO>> response = fetchData(() ->
                webClient.get().uri("/api/department").retrieve().bodyToMono(new ParameterizedTypeReference<>() {}));
        List<String> departmentNames = response
                .map(r -> r.getData().stream()
                        .map(DepartmentDTO::getDepartmentName).distinct().sorted().collect(Collectors.toList()))
                .orElse(Collections.emptyList());
        model.addAttribute("departments", departmentNames);
    }

    private void populateModalDropdowns(Model model) {
        // Fetch Locations
        Optional<ApiResponseDtowithoutpage<LocationDTO>> locResponse = fetchData(() ->
                webClient.get().uri("/api/locations").retrieve().bodyToMono(new ParameterizedTypeReference<>() {}));
        model.addAttribute("locations", locResponse.map(ApiResponseDtowithoutpage::getData).orElse(Collections.emptyList()));

        // Fetch Employees
        String url = UriComponentsBuilder.fromPath("/api/employees").queryParam("size", 1000).toUriString();
        Optional<Map<String, Object>> empResponse = fetchData(() ->
                webClient.get().uri(url).retrieve().bodyToMono(new ParameterizedTypeReference<>() {}));
        List<EmployeeDTO> allEmployees = empResponse.map(data -> {
            Map<String, Object> dataMap = (Map<String, Object>) data.get("data");
            List<Map<String, Object>> rawEmployees = (List<Map<String, Object>>) dataMap.get("employees");
            return rawEmployees.stream()
                    .map(map -> objectMapper.convertValue(map, EmployeeDTO.class))
                    .collect(Collectors.toList());
        }).orElse(Collections.emptyList());
        model.addAttribute("allEmployees", allEmployees);
    }

    private void populateEmployeesTable(String deptName, int page, int size, String sortBy, String sortDir, Model model) {
        String url = UriComponentsBuilder.fromPath("/api/department/{departmentName}/employees")
                .queryParam("page", page).queryParam("size", size)
                .queryParam("sortBy", sortBy).queryParam("sortDir", sortDir)
                .buildAndExpand(deptName).toUriString();
        Optional<ApiResponseDto<EmployeeDTO>> response = fetchData(() ->
                webClient.get().uri(url).retrieve().bodyToMono(new ParameterizedTypeReference<>() {}));
        if (response.isPresent() && response.get().getData() != null) {
            model.addAttribute("employeePage", response.get().getData());
        } else {
            model.addAttribute("error", "Could not fetch employees for department: " + deptName);
            ApiResponseDto.DataPage<EmployeeDTO> emptyPage = new ApiResponseDto.DataPage<>();
            emptyPage.setContent(Collections.emptyList()); emptyPage.setTotalPages(0); emptyPage.setNumber(page);
            emptyPage.setSize(size); emptyPage.setTotalElements(0L); emptyPage.setNumberOfElements(0);
            model.addAttribute("employeePage", emptyPage);
        }
    }

    private <T> Optional<T> fetchData(Supplier<Mono<T>> monoSupplier) {
        try {
            return Optional.ofNullable(monoSupplier.get().block());
        } catch (Exception e) {
            System.err.println("API call failed: " + e.getMessage());
            return Optional.empty();
        }
    }
}