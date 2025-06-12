package com.capgemini.hrmanagement.hr_portal.dto;

import lombok.Data;

@Data
public class EmployeeRegionDTO {
    private Long employeeId;
    private String firstName;
    private String lastName;
    private String departmentName;
    private String regionName;
}
