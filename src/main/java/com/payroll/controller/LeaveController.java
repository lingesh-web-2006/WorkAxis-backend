package com.payroll.controller;

import com.payroll.dto.LeaveDTOs.*;
import com.payroll.service.LeaveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/leaves")
public class LeaveController {

    @Autowired
    private LeaveService leaveService;

    @PostMapping("/request")
    public ResponseEntity<LeaveResponse> requestLeave(@RequestBody LeaveRequestDTO request) {
        return ResponseEntity.ok(leaveService.requestLeave(request));
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<LeaveResponse>> getEmployeeLeaves(@PathVariable("employeeId") Long employeeId) {
        return ResponseEntity.ok(leaveService.getEmployeeLeaves(employeeId));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<List<LeaveResponse>> getPendingLeaves() {
        return ResponseEntity.ok(leaveService.getPendingLeaves());
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<List<LeaveResponse>> getAllLeaves() {
        return ResponseEntity.ok(leaveService.getAllLeaves());
    }

    @PutMapping("/{id}/process")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LeaveResponse> processLeave(@PathVariable("id") Long id, @RequestBody LeaveActionDTO action) {
        return ResponseEntity.ok(leaveService.processLeave(id, action));
    }
}
