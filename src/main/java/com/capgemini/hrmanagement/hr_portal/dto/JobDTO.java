package com.capgemini.hrmanagement.hr_portal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobDTO {

    private String jobId;
    private String jobTitle;
    private BigDecimal minSalary;
    private BigDecimal maxSalary;
}
