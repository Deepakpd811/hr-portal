package com.capgemini.hrmanagement.hr_portal.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeRegionDTO {
    private BigDecimal id;
    private String fullName;
    private String department;
    private String jobTitle;
    private String city;
}
