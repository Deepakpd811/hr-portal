package com.capgemini.hrmanagement.hr_portal.dto;

import lombok.Data;

import java.util.List;

@Data
public class ApiResponseDtowithoutpage<T> {
    private String status;
    private String message;
    private String path;
    private List<T> data; // Use List<T> when T is RegionDTO
    private String timestamp;
}
