package com.capgemini.hrmanagement.hr_portal.dto;

import lombok.Data;
import org.springframework.beans.factory.parsing.Location;

@Data
public class DepartmentDTO {
    private Long departmentId;
    private String departmentName;
    private LocationDTO location;
}
