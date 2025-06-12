package com.capgemini.hrmanagement.hr_portal.service.departmentLocation;

import com.capgemini.hrmanagement.hr_portal.dto.departmentLocation.ApiResponseDto;
import com.capgemini.hrmanagement.hr_portal.dto.departmentLocation.ApiResponseDtowithoutpage;
import com.capgemini.hrmanagement.hr_portal.dto.departmentLocation.DepartmentDTO;
import com.capgemini.hrmanagement.hr_portal.dto.departmentLocation.LocationDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.core.ParameterizedTypeReference;

import java.util.List;

@Slf4j
@Service
public class DepartmentFrontendService {

    private final WebClient webClient;

    public DepartmentFrontendService(WebClient.Builder webClientBuilder,
                                     @Value("${api.base.url}") String baseUrl) {
        this.webClient = webClientBuilder
                .baseUrl(baseUrl)
                .build();
    }

    public List<LocationDTO> getAllLocations() {
        ApiResponseDtowithoutpage<LocationDTO> resp = webClient.get()
                .uri("/locations")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiResponseDtowithoutpage<LocationDTO>>() {})
                .block();

        return resp != null ? resp.getData() : List.of();
    }

    public List<DepartmentDTO> getDepartmentsByLocationId(Long locationId) {
        ApiResponseDtowithoutpage<DepartmentDTO> resp = webClient.get()
                .uri("/locations/departmentsByLocation/{locationId}", locationId)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiResponseDtowithoutpage<DepartmentDTO>>() {})
                .block();

        return resp != null ? resp.getData() : List.of();
    }

    public DepartmentDTO getDepartmentById(Long id) {
        ApiResponseDto<DepartmentDTO> resp = webClient.get()
                .uri("/department/{department_id}", id)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiResponseDto<DepartmentDTO>>() {})
                .block();

        return resp != null ? resp.getData().getContent().get(0) : null;
    }

    public void updateDepartment(Long id, DepartmentDTO departmentDTO) {
        webClient.put()
                .uri("/department/{department_id}", id)
                .bodyValue(departmentDTO)
                .retrieve()
                .toBodilessEntity()
                .block();
    }

    public void createDepartment(DepartmentDTO departmentDTO) {
        webClient.post()
                .uri("/department")
                .bodyValue(departmentDTO)
                .retrieve()
                .toBodilessEntity()
                .block();
    }
}
