package com.payroll.service;

import com.payroll.dto.LeaveDTOs.*;
import com.payroll.entity.Employee;
import com.payroll.entity.LeaveRequest;
import com.payroll.repository.EmployeeRepository;
import com.payroll.repository.LeaveRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class LeaveService {

    @Autowired
    private LeaveRepository leaveRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    public LeaveResponse requestLeave(LeaveRequestDTO request) {
        Employee employee = employeeRepository.findById(request.getEmployeeId())
            .orElseThrow(() -> new RuntimeException("Employee not found"));

        LeaveRequest leave = new LeaveRequest();
        leave.setEmployee(employee);
        leave.setStartDate(request.getStartDate());
        leave.setEndDate(request.getEndDate());
        leave.setLeaveType(request.getLeaveType());
        leave.setReason(request.getReason());
        leave.setStatus(LeaveRequest.LeaveStatus.PENDING);

        leave = leaveRepository.save(leave);
        return LeaveResponse.fromEntity(leave);
    }

    public List<LeaveResponse> getEmployeeLeaves(Long employeeId) {
        return leaveRepository.findByEmployeeIdOrderByCreatedAtDesc(employeeId)
            .stream().map(LeaveResponse::fromEntity).collect(Collectors.toList());
    }

    public List<LeaveResponse> getPendingLeaves() {
        return leaveRepository.findByStatusOrderByCreatedAtDesc(LeaveRequest.LeaveStatus.PENDING)
            .stream().map(LeaveResponse::fromEntity).collect(Collectors.toList());
    }

    public List<LeaveResponse> getAllLeaves() {
        return leaveRepository.findAll().stream()
            .map(LeaveResponse::fromEntity).collect(Collectors.toList());
    }

    public LeaveResponse processLeave(Long id, LeaveActionDTO action) {
        LeaveRequest leave = leaveRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Leave request not found"));

        leave.setStatus(LeaveRequest.LeaveStatus.valueOf(action.getStatus()));
        leave.setAdminRemarks(action.getAdminRemarks());

        leave = leaveRepository.save(leave);
        return LeaveResponse.fromEntity(leave);
    }
}
