package com.capgemini.hrmanagement.hr_portal.dto.departmentLocation;

import lombok.Data;
import java.util.List;

@Data
public class PageDTO<T> {
    private List<T> content;
    private int totalPages;
    private int number;
}
