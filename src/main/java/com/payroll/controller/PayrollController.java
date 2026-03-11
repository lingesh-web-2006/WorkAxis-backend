package com.payroll.controller;

import com.payroll.dto.PayrollDTOs.*;
import com.payroll.service.PayrollService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/payroll")
public class PayrollController {

    @Autowired
    private PayrollService payrollService;

    @PostMapping("/generate")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<PayrollResponse> generatePayroll(@RequestBody PayrollRequest request) {
        return ResponseEntity.ok(payrollService.generatePayroll(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PayrollResponse> getPayroll(@PathVariable("id") Long id) {
        return ResponseEntity.ok(payrollService.getPayrollById(id));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<PayrollResponse> updateStatus(@PathVariable("id") Long id,
                                                         @RequestParam("status") String status,
                                                         Authentication authentication) {
        String role = authentication.getAuthorities().toString();
        return ResponseEntity.ok(payrollService.updatePayrollStatus(id, status, role));
    }

    @GetMapping("/export/payslip/{id}")
    public void exportPayslip(@PathVariable("id") Long id, HttpServletResponse response) throws IOException {
        response.setContentType("application/pdf");
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=payslip_" + id + ".pdf";
        response.setHeader(headerKey, headerValue);
        payrollService.exportPayslip(id, response);
    }

    @GetMapping("/export/monthly")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public void exportMonthly(@RequestParam("month") int month, @RequestParam("year") int year, HttpServletResponse response) throws IOException {
        response.setContentType("application/octet-stream");
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=payroll_" + month + "_" + year + ".xlsx";
        response.setHeader(headerKey, headerValue);
        payrollService.exportMonthlyPayroll(month, year, response);
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<PayrollResponse>> getEmployeePayrolls(@PathVariable("employeeId") Long employeeId) {
        return ResponseEntity.ok(payrollService.getEmployeePayrolls(employeeId));
    }

    @GetMapping("/monthly")
    public ResponseEntity<Page<PayrollResponse>> getMonthlyPayroll(
        @RequestParam("month") int month,
        @RequestParam("year") int year,
        @RequestParam(name = "search", required = false) String search,
        @RequestParam(name = "page", defaultValue = "0") int page,
        @RequestParam(name = "size", defaultValue = "10") int size) {
        return ResponseEntity.ok(payrollService.getMonthlyPayroll(month, year, search, page, size));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletePayroll(@PathVariable("id") Long id) {
        payrollService.deletePayroll(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardStats> getDashboardStats(
        @RequestParam(name = "month", required = false) Integer month,
        @RequestParam(name = "year", required = false) Integer year) {
        int m = month != null ? month : LocalDate.now().getMonthValue();
        int y = year != null ? year : LocalDate.now().getYear();
        return ResponseEntity.ok(payrollService.getDashboardStats(m, y));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PayrollResponse>> getPendingPayrolls() {
        return ResponseEntity.ok(payrollService.getPendingPayrolls());
    }
}
