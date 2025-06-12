package com.capgemini.hrmanagement.hr_portal.client;


import com.capgemini.hrmanagement.hr_portal.dto.ApiResponseDtowithoutpageSingleData;
import com.capgemini.hrmanagement.hr_portal.dto.EmployeeDTO;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@FeignClient(name = "employeeClient", url = "${backend.url}")
public interface EmployeeClient {

    @GetMapping("/api/employees")
    ApiResponseDtowithoutpageSingleData<?> getAllEmployees(
            @RequestParam("page") int page,
            @RequestParam("size") int size
    );

    @GetMapping("/api/employees/{id}")
    ApiResponseDtowithoutpageSingleData getEmployeeById(@PathVariable("id") BigDecimal id);

    @PutMapping("/api/employees/{id}")
    ApiResponseDtowithoutpageSingleData updateEmployee(@PathVariable("id") BigDecimal id, @RequestBody EmployeeDTO employeeDTO);

    @PostMapping("/api/employees")
    ApiResponseDtowithoutpageSingleData createEmployee(@RequestBody EmployeeDTO dto);


}
