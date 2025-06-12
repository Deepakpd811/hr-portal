package com.capgemini.hrmanagement.hr_portal.controller;

import com.capgemini.hrmanagement.hr_portal.dto.ApiResponseDto;
import com.capgemini.hrmanagement.hr_portal.dto.ApiResponseDtowithoutpage;
import com.capgemini.hrmanagement.hr_portal.dto.DepartmentDTO;
import com.capgemini.hrmanagement.hr_portal.dto.EmployeeDTO;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class DepartmentPageController {

    private final WebClient webClient;

    public DepartmentPageController(WebClient webClient) {
        this.webClient = webClient;
    }

    // FIX #1: Mapped to the root URL "/" instead of "/index"
    @GetMapping("/")
    public String homePage() {
        return "index";
    }

    @GetMapping("/employees-by-department")
    public String getEmployeesByDepartment(
            @RequestParam(defaultValue = "Executive") String departmentName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "employeeId") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            Model model) {

        // 1. Fetch all departments for the dropdown
        List<String> departmentNames = Collections.emptyList();
        try {
            // FIX #2: Changed URI from "/api/departments" to "/api/department"
            ApiResponseDtowithoutpage<DepartmentDTO> departmentResponse = webClient.get()
                    .uri("/api/department")
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ApiResponseDtowithoutpage<DepartmentDTO>>() {})
                    .block();

            if (departmentResponse != null && departmentResponse.getData() != null) {
                departmentNames = departmentResponse.getData().stream()
                        .map(DepartmentDTO::getDepartmentName)
                        .distinct()
                        .sorted() // Added sorting for a better user experience
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            model.addAttribute("error", "Could not fetch departments. Is the backend running?");
        }

        // 2. Build the URL to fetch employees for the selected department
        String url = UriComponentsBuilder.fromPath("/api/department/{departmentName}/employees")
                .queryParam("page", page)
                .queryParam("size", size)
                .queryParam("sortBy", sortBy)
                .queryParam("sortDir", sortDir)
                .buildAndExpand(departmentName)
                .toUriString();

        // 3. Fetch the paginated list of employees
        ApiResponseDto<EmployeeDTO> employeeResponse = null;
        try {
            employeeResponse = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ApiResponseDto<EmployeeDTO>>() {})
                    .block();
        } catch (Exception e) {
            model.addAttribute("error", "Could not fetch employees for department: " + departmentName);
        }

        // 4. Add data to the model for rendering in Thymeleaf
        model.addAttribute("departments", departmentNames);
        model.addAttribute("departmentName", departmentName);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("size", size);
        model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");

        if (employeeResponse != null && employeeResponse.getData() != null) {
            model.addAttribute("employees", employeeResponse.getData().getContent());
            model.addAttribute("totalPages", employeeResponse.getData().getTotalPages());
            model.addAttribute("currentPage", employeeResponse.getData().getNumber());
        } else {
            model.addAttribute("employees", Collections.emptyList());
            model.addAttribute("totalPages", 0);
            model.addAttribute("currentPage", 0);
        }

        return "employees-by-department";
    }
}
