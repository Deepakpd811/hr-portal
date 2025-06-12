package com.capgemini.hrmanagement.hr_portal.controller;

import com.capgemini.hrmanagement.hr_portal.dto.*;
import com.capgemini.hrmanagement.hr_portal.dto.departmentLocation.ApiResponseDto;
import com.capgemini.hrmanagement.hr_portal.dto.departmentLocation.ApiResponseDtowithoutpage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/region-ui")
public class RegionPageController {

    private final WebClient webClient;

    public RegionPageController(WebClient.Builder webClientBuilder,
                                @Value("${api.base.url}") String baseUrl) {
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
    }

    @GetMapping
    public String getEmployeesByRegion(
            @RequestParam(defaultValue = "EUROPE") String regionName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "employeeId") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            Model model) {

        ApiResponseDtowithoutpage<RegionDTO> regionResp = webClient.get()
                .uri("/regions")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiResponseDtowithoutpage<RegionDTO>>() {})
                .block();

        List<String> regionNames = regionResp != null ?
                regionResp.getData()
                        .stream()
                        .map(RegionDTO::getRegionName)
                        .distinct()
                        .collect(Collectors.toList())
                : Collections.emptyList();

        String url = String.format("/reports/employees-by-region/%s?page=%d&size=%d&sortBy=%s&sortDir=%s",
                regionName, page, size, sortBy, sortDir);

        ApiResponseDto<EmployeeRegionDTO> resp = webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiResponseDto<EmployeeRegionDTO>>() {})
                .block();

        model.addAttribute("regions", regionNames);
        model.addAttribute("regionName", regionName);
        if (resp != null && resp.getData() != null) {
            model.addAttribute("employees", resp.getData().getContent());
            model.addAttribute("totalPages", resp.getData().getTotalPages());
            model.addAttribute("currentPage", resp.getData().getNumber());
        } else {
            model.addAttribute("employees", Collections.emptyList());
        }

        return "employees-by-region";
    }
}
