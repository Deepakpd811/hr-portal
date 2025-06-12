package com.capgemini.hrmanagement.hr_portal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentDTO {
    private BigDecimal departmentId;
    private String departmentName;
    private String managerName;
    private String locationCity;

}
