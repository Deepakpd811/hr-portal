package com.capgemini.hrmanagement.hr_portal.controller;

import com.capgemini.hrmanagement.hr_portal.dto.ApiResponseDto;
import com.capgemini.hrmanagement.hr_portal.dto.ApiResponseDtowithoutpage;
import com.capgemini.hrmanagement.hr_portal.dto.DepartmentDTO;
import com.capgemini.hrmanagement.hr_portal.dto.EmployeeDTO;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class DepartmentPageController {

    private final WebClient webClient;

    public DepartmentPageController(WebClient webClient) {
        this.webClient = webClient;
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

        // Fetching all departments for the dropdown
        List<String> departmentNames = Collections.emptyList();
        try {
            ApiResponseDtowithoutpage<DepartmentDTO> departmentResponse = webClient.get()
                    .uri("/api/department")
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ApiResponseDtowithoutpage<DepartmentDTO>>() {})
                    .block();

            if (departmentResponse != null && departmentResponse.getData() != null) {
                departmentNames = departmentResponse.getData().stream()
                        .map(DepartmentDTO::getDepartmentName)
                        .distinct()
                        .sorted()
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            model.addAttribute("error", "Could not fetch departments. Is the backend running?");
        }

        String url = UriComponentsBuilder.fromPath("/api/department/{departmentName}/employees")
                .queryParam("page", page)
                .queryParam("size", size)
                .queryParam("sortBy", sortBy)
                .queryParam("sortDir", sortDir)
                .buildAndExpand(selectedDepartmentName)
                .toUriString();

        // Fetching paginated list of employees
        ApiResponseDto<EmployeeDTO> employeeResponse = null;
        try {
            employeeResponse = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ApiResponseDto<EmployeeDTO>>() {})
                    .block();
        } catch (Exception e) {
            model.addAttribute("error", "Could not fetch employees for department: " + selectedDepartmentName);
        }

        //Adding data to the model
        model.addAttribute("departments", departmentNames);
        model.addAttribute("selectedDepartmentName", selectedDepartmentName);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");


        if (employeeResponse != null && employeeResponse.getData() != null) {
            model.addAttribute("employeePage", employeeResponse.getData());
        } else {
            ApiResponseDto.DataPage<EmployeeDTO> emptyPage = new ApiResponseDto.DataPage<>();
            emptyPage.setContent(Collections.emptyList());
            emptyPage.setTotalPages(0);
            emptyPage.setNumber(page);
            emptyPage.setSize(size);
            emptyPage.setTotalElements(0L);
            emptyPage.setNumberOfElements(0);

            model.addAttribute("employeePage", emptyPage);
        }

        return "employees_by_department";
    }
}