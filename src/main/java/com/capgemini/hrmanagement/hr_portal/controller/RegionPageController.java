package com.capgemini.hrmanagement.hr_portal.controller;

import com.capgemini.hrmanagement.hr_portal.dto.*;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/region-ui")
public class RegionPageController {

    private final WebClient webClient;

    public RegionPageController(WebClient webClient) {
        this.webClient = webClient;
    }

    @GetMapping
    public String getEmployeesByRegion(
            @RequestParam(defaultValue = "EUROPE") String regionName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "employeeId") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            Model model) {


        // Fetch regions from backend
        ApiResponseDtowithoutpage<RegionDTO> regionResponse = webClient.get()
                .uri("/api/regions")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiResponseDtowithoutpage<RegionDTO>>() {})
                .block();

        List<String> regionNames = regionResponse.getData().stream()
                .map(RegionDTO::getRegionName)
                .distinct()
                .toList(); // Optional: filter duplicates


        String url = String.format("/api/reports/employees-by-region/%s?page=%d&size=%d&sortBy=%s&sortDir=%s",
                regionName, page, size, sortBy, sortDir);

        ApiResponseDto<EmployeeRegionDTO> response = webClient
                .get()
                .uri(url)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiResponseDto<EmployeeRegionDTO>>() {})
                .block();

        model.addAttribute("regions", regionNames);
        model.addAttribute("regionName", regionName);

        if (response != null && response.getData() != null) {
            model.addAttribute("employees", response.getData().getContent());
            model.addAttribute("regionName", regionName);
            model.addAttribute("totalPages", response.getData().getTotalPages());
            model.addAttribute("currentPage", response.getData().getNumber());
        } else {
            model.addAttribute("employees", List.of());
            model.addAttribute("regionName", regionName);
        }

        return "employees-by-region";
    }





    @GetMapping("/edit/{id}")
    public String editEmployeeDepartment(@PathVariable BigDecimal id, Model model) {
        // Fetch employee details
        EmployeeDetailDTO employee= webClient.get()
                .uri("/api/employees/" + id)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiResponseDtowithoutpageSingleData<EmployeeDetailDTO>>() {})
                .block()
                .getData();


        // Fetch all departments
        ApiResponseDtowithoutpage<DepartmentDTO> response = webClient.get()
                .uri("/api/department")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiResponseDtowithoutpage<DepartmentDTO>>() {})
                .block();

        System.out.println(response);
        System.out.println("emp"+employee);

        model.addAttribute("employee", employee);
        model.addAttribute("departments", response.getData());
        return "edit-employee";
    }

    @PostMapping("/update-department/{id}")
    public String updateEmployeeDepartment(@PathVariable BigDecimal id, @RequestParam BigDecimal departmentId) {
        UpdateEmployeeDepartmentDTO dto = new UpdateEmployeeDepartmentDTO();
        dto.setDepartmentId(departmentId);



        try{
            webClient.put()
                    .uri("/api/employee-dept/" + id.stripTrailingZeros().toPlainString())
                    .bodyValue(dto)
                    .retrieve()
                    .toBodilessEntity()
                    .block();

        }catch (Exception e){
            System.out.println("error in fetching " + e);
        }


        return "redirect:/region-ui";
    }







}
