package com.capgemini.hrmanagement.hr_portal.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import feign.FeignException;

import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private final ObjectMapper objectMapper;

    public GlobalExceptionHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @ExceptionHandler(FeignException.class)
    public String handleFeignException(FeignException e, Model model) {
        logger.error("Feign client error occurred", e);
        String userFriendlyMessage = "An unexpected error occurred while updating the employee.";

        try {
            // Parse the Feign exception response body (JSON)
            String responseBody = e.contentUTF8();
            Map<String, Object> errorResponse = objectMapper.readValue(responseBody, Map.class);
            String errorMessage = (String) errorResponse.get("message");

            // Customize message based on the error content
            if (errorMessage != null) {
                if (errorMessage.contains("Job not found")) {
                    userFriendlyMessage = "Failed to update employee: The specified Job ID is invalid.";
                } else if (errorMessage.contains("Department not found")) {
                    userFriendlyMessage = "Failed to update employee: The specified Department ID is invalid.";
                } else {
                    userFriendlyMessage = "Failed to update employee: " + errorMessage;
                }
            }
        } catch (Exception ex) {
            logger.error("Failed to parse Feign exception response", ex);
            // Fallback to generic message if parsing fails
        }

        model.addAttribute("error", userFriendlyMessage);
        return "error";
    }

    @ExceptionHandler(Exception.class)
    public String handleException(Exception e, Model model) {
        logger.error("Unexpected error occurred", e);
        model.addAttribute("error", "An unexpected error occurred: " + e.getMessage());
        return "error";
    }
}