package com.capgemini.hrmanagement.hr_portal.dto;

import lombok.Data;

import java.util.List;

@Data
public class ApiResponseDtowithoutpageSingleData<T> {
    private T data;
    private String status;
    private String message;
    private String path;
}

