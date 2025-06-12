package com.capgemini.hrmanagement.hr_portal.client;


import com.capgemini.hrmanagement.hr_portal.dto.ApiResponseDtowithoutpageSingleData;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "department-service" , url = "${backend.url}")
public interface DepartmentClient {

    @GetMapping("/api/department")
    ResponseEntity<ApiResponseDtowithoutpageSingleData> getAllDepartments();
}