package com.payroll.dto;

import com.payroll.entity.Employee;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class EmployeeDTOs {

    @Data
    public static class EmployeeRequest {
        private String name;
        private String email;
        private String department;
        private String position;
        @com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate joiningDate;
        private BigDecimal basicSalary;
        private String phone;
        private String address;
        private Employee.EmploymentStatus status;
    }

    @Data
    public static class EmployeeResponse {
        private Long id;
        private String name;
        private String email;
        private String department;
        private String position;
        @com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate joiningDate;
        private BigDecimal basicSalary;
        private String phone;
        private String address;
        private Employee.EmploymentStatus status;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public static EmployeeResponse fromEntity(Employee emp) {
            EmployeeResponse res = new EmployeeResponse();
            res.setId(emp.getId());
            res.setName(emp.getName());
            res.setEmail(emp.getEmail());
            res.setDepartment(emp.getDepartment());
            res.setPosition(emp.getPosition());
            res.setJoiningDate(emp.getJoiningDate());
            res.setBasicSalary(emp.getBasicSalary());
            res.setPhone(emp.getPhone());
            res.setAddress(emp.getAddress());
            res.setStatus(emp.getStatus());
            res.setCreatedAt(emp.getCreatedAt());
            res.setUpdatedAt(emp.getUpdatedAt());
            return res;
        }
    }

    @Data
    public static class PagedEmployeeResponse {
        private java.util.List<EmployeeResponse> content;
        private int page;
        private int size;
        private long totalElements;
        private int totalPages;
        private boolean last;
    }
}
