package com.capgemini.hrmanagement.hr_portal.controller;

import com.capgemini.hrmanagement.hr_portal.dto.ApiResponseDto;
import com.capgemini.hrmanagement.hr_portal.dto.ApiResponseDtowithoutpage;
import com.capgemini.hrmanagement.hr_portal.dto.EmployeeJobDTO;
import com.capgemini.hrmanagement.hr_portal.dto.JobDTO;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/job-ui")
public class JobPageController {

    private final WebClient webClient;

    public JobPageController(WebClient webClient) {
        this.webClient = webClient;
    }

    // Show employee list by job (with pagination & sorting)
    @GetMapping
    public String getEmployeesByJob(
            @RequestParam(defaultValue = "AD_PRES") String jobId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "employeeId") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            Model model,
            @ModelAttribute("message") String message) {

        // Fetch jobs for dropdown
        ApiResponseDtowithoutpage<JobDTO> jobResponse = webClient.get()
                .uri("/api/jobs")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiResponseDtowithoutpage<JobDTO>>() {})
                .block();

        List<JobDTO> jobs = jobResponse != null ? jobResponse.getData() : List.of();

        // Fetch employees by selected job
        String url = String.format("/api/jobs/%s/employees?page=%d&size=%d&sortBy=%s&sortDir=%s",
                jobId, page, size, sortBy, sortDir);

        ApiResponseDto<EmployeeJobDTO> response = webClient
                .get()
                .uri(url)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiResponseDto<EmployeeJobDTO>>() {})
                .block();

        model.addAttribute("jobs", jobs);
        model.addAttribute("jobId", jobId);
        model.addAttribute("message", message);

        if (response != null && response.getData() != null) {
            model.addAttribute("employees", response.getData().getContent());
            model.addAttribute("totalPages", response.getData().getTotalPages());
            model.addAttribute("currentPage", response.getData().getNumber());
        } else {
            model.addAttribute("employees", List.of());
            model.addAttribute("totalPages", 0);
            model.addAttribute("currentPage", 0);
        }

        return "employees-by-job";
    }

    // Show job creation form
    @GetMapping("/new")
    public String showCreateJobForm(Model model) {
        model.addAttribute("jobDTO", new JobDTO());
        return "create-job";
    }

    // Handle job creation (calls backend POST)
    @PostMapping("/create")
    public String createJob(@ModelAttribute JobDTO jobDTO, RedirectAttributes redirectAttributes) {
        try {
            ApiResponseDto<?> response = webClient.post()
                    .uri("/api/jobs")
                    .bodyValue(jobDTO)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ApiResponseDto<?>>() {})
                    .block();

            redirectAttributes.addFlashAttribute("message",
                    response != null && response.getMessage() != null
                            ? response.getMessage()
                            : "Job created successfully.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("message", "Error creating job: " + ex.getMessage());
        }

        return "redirect:/job-ui";
    }

    // Show job update form
    @GetMapping("/update")
    public String showUpdateJobForm(@RequestParam("jobId") String jobId, Model model) {
        ApiResponseDtowithoutpage<JobDTO> response = webClient.get()
                .uri("/api/jobs")  // <-- this should be /api/jobs/{jobId}, not all jobs
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiResponseDtowithoutpage<JobDTO>>() {})
                .block();

        List<JobDTO> jobList = response != null ? response.getData() : List.of();

        JobDTO jobDTO = jobList.stream()
                .filter(job -> jobId.equals(job.getJobId()))
                .findFirst()
                .orElse(null);



        // ✅ Log for debug
        System.out.println("Loaded for update: " + (jobDTO != null ? jobDTO.getJobId() : "null"));

        model.addAttribute("jobDTO", jobDTO);
        return "update-job";
    }



    @PostMapping("/update")
    public String updateJob(@ModelAttribute JobDTO jobDTO, RedirectAttributes redirectAttributes) {

        // Debug: Log values received from the form
        System.out.println("Updating job with ID: " + jobDTO.getJobId());
        System.out.println("Title: " + jobDTO.getJobTitle());
        System.out.println("Min Salary: " + jobDTO.getMinSalary());
        System.out.println("Max Salary: " + jobDTO.getMaxSalary());

        try {
            ApiResponseDto<?> response = webClient.put()
                    .uri("/api/jobs/{id}", jobDTO.getJobId())
                    .bodyValue(jobDTO)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ApiResponseDto<?>>() {})
                    .block();

            redirectAttributes.addFlashAttribute("message",
                    response != null && response.getMessage() != null
                            ? response.getMessage()
                            : "Job updated successfully.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("message", "Error updating job: " + ex.getMessage());
        }

        return "redirect:/job-ui";
    }
}