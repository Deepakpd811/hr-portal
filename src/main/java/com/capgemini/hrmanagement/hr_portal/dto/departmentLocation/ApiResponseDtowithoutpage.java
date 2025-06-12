package com.capgemini.hrmanagement.hr_portal.dto.departmentLocation;

import lombok.Data;
import java.util.List;

@Data
public class ApiResponseDtowithoutpage<T> {
    private List<T> data;
}
