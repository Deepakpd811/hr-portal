package com.capgemini.hrmanagement.hr_portal.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CreateDepartmentRequestDTO {

    private BigDecimal departmentId;
    private String departmentName;
    private BigDecimal managerId;
    private BigDecimal locationId;

}
