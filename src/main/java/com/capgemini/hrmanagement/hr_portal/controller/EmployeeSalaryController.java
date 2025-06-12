package com.capgemini.hrmanagement.hr_portal.controller;

import com.capgemini.hrmanagement.hr_portal.config.WebClientConfig;
import com.capgemini.hrmanagement.hr_portal.dto.EmployeeDTO;
import com.capgemini.hrmanagement.hr_portal.dto.ApiResponseDtowithoutpage;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/salary-ui")
@RequiredArgsConstructor
public class EmployeeSalaryController {

    private final WebClient webClient;

    // GET: Display employees with salary > amount
    @GetMapping
    public String getEmployeesWithHighSalary(
            @RequestParam(defaultValue = "5000") BigDecimal amount,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            Model model) {

        // Request one extra item to detect if next page exists
        int fetchSize = size + 1;

        String uri = String.format("/api/employees/salary_greater_than/%s?page=%d&size=%d", amount, page, fetchSize);

        ApiResponseDtowithoutpage<EmployeeDTO> response = webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiResponseDtowithoutpage<EmployeeDTO>>() {})
                .block();

        List<EmployeeDTO> employees = response != null ? response.getData() : List.of();

        // Check if there is next page
        boolean hasNext = employees.size() == fetchSize;

        // Show only 'size' items on the current page
        if (hasNext) {
            employees = employees.subList(0, size);
        }

        model.addAttribute("employees", employees);
        model.addAttribute("amount", amount);
        model.addAttribute("page", page);
        model.addAttribute("size", size);
        model.addAttribute("hasNext", hasNext);

        return "employee-salary-list";
    }



    //  GET: Show insert form
    @GetMapping("/insert")
    public String showInsertForm(Model model) {
        model.addAttribute("employee", new EmployeeDTO());
        return "insert-employee";
    }

    // POST: Create new employee
    @PostMapping
    public String insertEmployee(@ModelAttribute EmployeeDTO employeeDTO) {
        webClient.post()
                .uri("/api/employees")
                .body(Mono.just(employeeDTO), EmployeeDTO.class)
                .retrieve()
                .bodyToMono(Void.class)
                .block();

        return "redirect:/salary-ui";
    }

    // GET: Show edit form
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable BigDecimal id, Model model) {
        EmployeeDTO employee = webClient.get()
                .uri("/api/employees/{id}", id)
                .retrieve()
                .bodyToMono(EmployeeDTO.class)
                .block();

        model.addAttribute("employee", employee);
        return "edit-employee";
    }

    // POST: Update existing employee
    @PostMapping("/edit/{id}")
    public String updateEmployee(@PathVariable BigDecimal id, @ModelAttribute EmployeeDTO employeeDTO) {
        webClient.put()
                .uri("/api/employees/{id}", id)
                .body(Mono.just(employeeDTO), EmployeeDTO.class)
                .retrieve()
                .bodyToMono(Void.class)
                .block();

        return "redirect:/salary-ui";
    }
}
