package com.capgemini.hrmanagement.hr_portal.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
public class EmployeeDTO {
    private BigDecimal employeeId;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private LocalDate hireDate;
    private BigDecimal salary;
    private BigDecimal commissionPct;
    private String job_Id;
    private BigDecimal department_Id;
    private BigDecimal manager_Id;
}

