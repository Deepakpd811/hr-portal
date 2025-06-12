package com.capgemini.hrmanagement.hr_portal.dto;

import lombok.Data;

import java.util.List;

@Data
public class ApiResponseDto<T> {
    private String status;
    private String message;
    private String path;
    private DataPage<T> data;

    @Data
    public static class DataPage<T> {
        private List<T> content;
        private int totalPages;
        private long totalElements;     // Add this for 'total records'
        private int number;             // Current page number (0-indexed)
        private int size;
        private int numberOfElements;

        public boolean isEmpty() {
            return content == null || content.isEmpty();
        }
    }
}
