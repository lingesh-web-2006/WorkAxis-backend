package com.payroll.dto;

import com.payroll.entity.Payroll;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class PayrollDTOs {

    @Data
    public static class PayrollRequest {
        private Long employeeId;
        private Integer payMonth;
        private Integer payYear;
        private BigDecimal basicSalary;
        private BigDecimal bonus;
        private BigDecimal otherAllowances;
        private BigDecimal otherDeductions;
        private String remarks;
    }

    @Data
    public static class PayrollResponse {
        private Long id;
        private Long employeeId;
        private String employeeName;
        private String employeeEmail;
        private String department;
        private String position;
        private Integer payMonth;
        private Integer payYear;
        private BigDecimal basicSalary;
        private BigDecimal hra;
        private BigDecimal bonus;
        private BigDecimal otherAllowances;
        private BigDecimal totalAllowances;
        private BigDecimal taxDeduction;
        private BigDecimal pfDeduction;
        private BigDecimal otherDeductions;
        private BigDecimal totalDeductions;
        private BigDecimal netSalary;
        private Payroll.PayrollStatus status;
        private String remarks;
        private LocalDateTime createdAt;

        public static PayrollResponse fromEntity(Payroll p) {
            PayrollResponse res = new PayrollResponse();
            res.setId(p.getId());
            res.setEmployeeId(p.getEmployee().getId());
            res.setEmployeeName(p.getEmployee().getName());
            res.setEmployeeEmail(p.getEmployee().getEmail());
            res.setDepartment(p.getEmployee().getDepartment());
            res.setPosition(p.getEmployee().getPosition());
            res.setPayMonth(p.getPayMonth());
            res.setPayYear(p.getPayYear());
            res.setBasicSalary(p.getBasicSalary());
            res.setHra(p.getHra());
            res.setBonus(p.getBonus());
            res.setOtherAllowances(p.getOtherAllowances());
            res.setTotalAllowances(p.getTotalAllowances());
            res.setTaxDeduction(p.getTaxDeduction());
            res.setPfDeduction(p.getPfDeduction());
            res.setOtherDeductions(p.getOtherDeductions());
            res.setTotalDeductions(p.getTotalDeductions());
            res.setNetSalary(p.getNetSalary());
            res.setStatus(p.getStatus());
            res.setRemarks(p.getRemarks());
            res.setCreatedAt(p.getCreatedAt());
            return res;
        }
    }

    @Data
    public static class DashboardStats {
        private long totalEmployees;
        private long activeEmployees;
        private BigDecimal totalPayrollThisMonth;
        private BigDecimal totalPayrollThisYear;
        private long payrollsProcessedThisMonth;
        private List<MonthlySummary> monthlyTrend;
    }

    @Data
    public static class MonthlySummary {
        private int month;
        private String monthName;
        private BigDecimal totalAmount;
        private long employeeCount;

        public MonthlySummary(int month, String monthName, BigDecimal totalAmount) {
            this.month = month;
            this.monthName = monthName;
            this.totalAmount = totalAmount;
        }
    }
}
