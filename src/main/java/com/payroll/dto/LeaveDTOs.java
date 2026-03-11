package com.payroll.dto;

import com.payroll.entity.LeaveRequest;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class LeaveDTOs {

    @Data
    public static class LeaveRequestDTO {
        private Long employeeId;
        private LocalDate startDate;
        private LocalDate endDate;
        private LeaveRequest.LeaveType leaveType;
        private String reason;
    }

    @Data
    public static class LeaveActionDTO {
        private String status;
        private String adminRemarks;
    }

    @Data
    public static class LeaveResponse {
        private Long id;
        private Long employeeId;
        private String employeeName;
        private String department;
        private LocalDate startDate;
        private LocalDate endDate;
        private LeaveRequest.LeaveType leaveType;
        private String reason;
        private LeaveRequest.LeaveStatus status;
        private String adminRemarks;
        private LocalDateTime createdAt;

        public static LeaveResponse fromEntity(LeaveRequest leave) {
            LeaveResponse res = new LeaveResponse();
            res.setId(leave.getId());
            res.setEmployeeId(leave.getEmployee().getId());
            res.setEmployeeName(leave.getEmployee().getName());
            res.setDepartment(leave.getEmployee().getDepartment());
            res.setStartDate(leave.getStartDate());
            res.setEndDate(leave.getEndDate());
            res.setLeaveType(leave.getLeaveType());
            res.setReason(leave.getReason());
            res.setStatus(leave.getStatus());
            res.setAdminRemarks(leave.getAdminRemarks());
            res.setCreatedAt(leave.getCreatedAt());
            return res;
        }
    }
}
