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
@RequestMapping("/departments-by-location")
public class DepartmentByLocationPageController {

    private final WebClient webClient;

    public DepartmentByLocationPageController(WebClient webClient) {
        this.webClient = webClient;
    }

    @GetMapping
    public String showDepartmentsByLocation(@RequestParam(required = false) BigDecimal locationId,
                                            Model model) {

        // 1. Fetch all locations for the dropdown
        ApiResponseDtowithoutpage<LocationDTO> locationListResponse = webClient.get()
                .uri("/api/locations")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiResponseDtowithoutpage<LocationDTO>>() {})
                .block();

        List<LocationDTO> locations = locationListResponse != null ? locationListResponse.getData() : List.of();
        model.addAttribute("locations", locations);

        // 2. If a locationId is selected, fetch departments for that location
        if (locationId != null) {
            try {
                String url = String.format("/api/locations/departmentsByLocation/%s", locationId);

                ApiResponseDtowithoutpage<DepartmentLocationDTO> departmentResponse = webClient.get()
                        .uri(url)
                        .retrieve()
                        .bodyToMono(new ParameterizedTypeReference<ApiResponseDtowithoutpage<DepartmentLocationDTO>>() {})
                        .block();

                List<DepartmentLocationDTO> departments = departmentResponse != null ? departmentResponse.getData() : List.of();

                model.addAttribute("departments", departments);
                model.addAttribute("selectedLocationId", locationId);

                if (departments.isEmpty()) {
                    model.addAttribute("error", "No departments found for the selected location.");
                }

            } catch (Exception ex) {
                model.addAttribute("departments", List.of());
                model.addAttribute("error", "Error fetching departments: " + ex.getMessage());
            }
        } else {
            model.addAttribute("departments", List.of());
        }

        return "location/departments-by-location"; // Thymeleaf template name
    }




    @GetMapping("/departments/edit/{id}")
    public String editDepartmentForm(@PathVariable("id") BigDecimal id, Model model) {
        try {
            // Call API to get the department details
            ApiResponseDtowithoutpageSingleData<DepartmentDTO> departmentDto = webClient.get()
                    .uri("/api/department/{id}", id)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ApiResponseDtowithoutpageSingleData<DepartmentDTO>>() {})
                    .block();


            // Call API to get list of locations
            ApiResponseDtowithoutpage<LocationDTO> locationResponse = webClient.get()
                    .uri("/api/locations")
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ApiResponseDtowithoutpage<LocationDTO>>() {})
                    .block();

            List<LocationDTO> locationDTOs = locationResponse != null ? locationResponse.getData() : List.of();

            System.out.println(locationDTOs);

            // Add to model
            model.addAttribute("department", departmentDto.getData());
            model.addAttribute("locations", locationDTOs);

            return "location/edit-department";

        } catch (Exception e) {
            model.addAttribute("error", "Error fetching department data: " + e.getMessage());
            return "error"; // Return to an error view (create this page if needed)
        }
    }


    @PostMapping("/update-department/{id}") // Changed URL mapping
    public String updateDepartment(@PathVariable("id") BigDecimal id, // Added PathVariable
                                   @ModelAttribute("department") DepartmentDTO dto) {
        try {
            // Ensure the ID from the path matches the ID in the DTO if necessary for validation
            // dto.setDepartmentId(id); // Optional: ensure consistency if frontend doesn't send it in body

            webClient.put()
                    // Use the @PathVariable 'id' in the URI, similar to your example
                    // Assuming your backend PUT /api/department/{id} expects the updated DepartmentDTO in the body
                    .uri("/api/department/" + id.stripTrailingZeros().toPlainString()) // Example from your provided code
                    .bodyValue(dto) // Sends the full DepartmentDTO (with updated name, locationId)
                    .retrieve()
                    .toBodilessEntity() // Similar to your example's toBodilessEntity()
                    .block();


            return "redirect:/departments-by-location";
        } catch (Exception e) {
            System.out.println("Error updating department: " + e.getMessage()); // Print to console for debugging

            // Redirect back to the edit page with the department ID in case of an error
            return "redirect:/departments-by-location"; // Use 'id' from PathVariable
        }
    }
}





