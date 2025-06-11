package com.capgemini.hrmanagement.hr_portal.controller;

import com.capgemini.hrmanagement.hr_portal.dto.ApiResponseDto;
import com.capgemini.hrmanagement.hr_portal.dto.ApiResponseDtowithoutpage;
import com.capgemini.hrmanagement.hr_portal.dto.EmployeeRegionDTO;
import com.capgemini.hrmanagement.hr_portal.dto.RegionDTO;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.function.client.WebClient;

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
}
