package com.capgemini.hrmanagement.hr_portal.controller;

import com.capgemini.hrmanagement.hr_portal.dto.DepartmentDTO;
import com.capgemini.hrmanagement.hr_portal.service.DepartmentFrontendService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class LocationViewController {

    private final DepartmentFrontendService departmentFrontendService;

    public LocationViewController(DepartmentFrontendService departmentFrontendService) {
        this.departmentFrontendService = departmentFrontendService;
    }

    @GetMapping("/location")
    public String showLocationSelection(Model model) {
        model.addAttribute("locations", departmentFrontendService.getAllLocations());
        return "index";
    }

    @GetMapping("/departments")
    public String showDepartments(@RequestParam("locationId") Long locationId, Model model) {
        List<DepartmentDTO> departments = departmentFrontendService.getDepartmentsByLocationId(locationId);
        model.addAttribute("departments", departments);
        model.addAttribute("locationId", locationId);
        return "departments";
    }

    @GetMapping("/departments/edit/{id}")
    public String editDepartmentForm(@PathVariable Long id, Model model) {
        DepartmentDTO department = departmentFrontendService.getDepartmentById(id);
        model.addAttribute("department", department);
        model.addAttribute("locations", departmentFrontendService.getAllLocations());
        return "edit";
    }

    @PostMapping("/departments/update/{id}")
    public String updateDepartment(@PathVariable Long id, @ModelAttribute DepartmentDTO departmentDTO) {
        departmentFrontendService.updateDepartment(id, departmentDTO);
        return "redirect:/";
    }

    @GetMapping("/departments/add")
    public String addDepartmentForm(Model model) {
        model.addAttribute("department", new DepartmentDTO());
        model.addAttribute("locations", departmentFrontendService.getAllLocations());
        return "addDepartment";
    }

    @PostMapping("/departments/create")
    public String createDepartment(@ModelAttribute DepartmentDTO departmentDTO) {
        departmentFrontendService.createDepartment(departmentDTO);
        return "redirect:/";
    }
}
