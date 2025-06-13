package com.capgemini.hrmanagement.hr_portal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeJobDTO {

        private BigDecimal employeeId;
        private String firstName;
        private String lastName;
        private BigDecimal salary;
        private String email;
    }

