package com.capgemini.hrmanagement.hr_portal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentLocationDTO {
    private BigDecimal departmentId;
    private String departmentName;
    private BigDecimal locationId;
    private String city;
    private String managerName;
    private BigDecimal managerId;
}

