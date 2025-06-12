package com.capgemini.hrmanagement.hr_portal.dto;

import lombok.Data;

@Data
public class ApiResponseDto<T> {
    private PageDTO<T> data;
}
