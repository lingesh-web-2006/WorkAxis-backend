package com.payroll.controller;

import com.payroll.dto.EmployeeDTOs.*;
import com.payroll.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<EmployeeResponse> createEmployee(@RequestBody EmployeeRequest request) {
        return ResponseEntity.ok(employeeService.createEmployee(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<EmployeeResponse> updateEmployee(@PathVariable("id") Long id,
                                                           @RequestBody EmployeeRequest request) {
        return ResponseEntity.ok(employeeService.updateEmployee(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteEmployee(@PathVariable("id") Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponse> getEmployee(@PathVariable("id") Long id) {
        return ResponseEntity.ok(employeeService.getEmployee(id));
    }

    @GetMapping
    public ResponseEntity<PagedEmployeeResponse> getAllEmployees(
        @RequestParam(name = "page", defaultValue = "0") int page,
        @RequestParam(name = "size", defaultValue = "10") int size,
        @RequestParam(name = "search", required = false) String search,
        @RequestParam(name = "sortBy", defaultValue = "id") String sortBy,
        @RequestParam(name = "sortDir", defaultValue = "asc") String sortDir) {
        return ResponseEntity.ok(employeeService.getAllEmployees(page, size, search, sortBy, sortDir));
    }

    @GetMapping("/departments")
    public ResponseEntity<List<String>> getDepartments() {
        return ResponseEntity.ok(employeeService.getAllDepartments());
    }

    @GetMapping("/active")
    public ResponseEntity<List<EmployeeResponse>> getActiveEmployees() {
        return ResponseEntity.ok(employeeService.getActiveEmployees());
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<EmployeeResponse>> getPendingEmployees() {
        return ResponseEntity.ok(employeeService.getPendingEmployees());
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EmployeeResponse> approveEmployee(@PathVariable("id") Long id) {
        return ResponseEntity.ok(employeeService.approveEmployee(id));
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EmployeeResponse> rejectEmployee(@PathVariable("id") Long id) {
        return ResponseEntity.ok(employeeService.rejectEmployee(id));
    }

    @GetMapping("/distribution")
    public ResponseEntity<java.util.List<java.util.Map<String, Object>>> getDepartmentDistribution() {
        return ResponseEntity.ok(employeeService.getDepartmentDistribution());
    }

    @GetMapping("/status-distribution")
    public ResponseEntity<java.util.List<java.util.Map<String, Object>>> getStatusDistribution() {
        return ResponseEntity.ok(employeeService.getStatusDistribution());
    }

    @GetMapping("/dept-stats")
    public ResponseEntity<java.util.List<java.util.Map<String, Object>>> getDepartmentSalaryStats() {
        return ResponseEntity.ok(employeeService.getDepartmentSalaryStats());
    }
}
