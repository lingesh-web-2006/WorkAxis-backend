package com.payroll.service;

import com.payroll.dto.PayrollDTOs.*;
import com.payroll.entity.Employee;
import com.payroll.entity.Payroll;
import com.payroll.repository.EmployeeRepository;
import com.payroll.repository.PayrollRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Month;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class PayrollService {

    @Autowired
    private PayrollRepository payrollRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private PayslipService payslipService;

    @Autowired
    private HttpServletResponse response;

    // Salary Calculation Constants
    private static final BigDecimal HRA_RATE = new BigDecimal("0.40");         // 40% of basic
    private static final BigDecimal PF_RATE = new BigDecimal("0.12");          // 12% of basic
    private static final BigDecimal TAX_RATE_LOW = new BigDecimal("0.05");     // 5% for < 5L
    private static final BigDecimal TAX_RATE_MID = new BigDecimal("0.20");     // 20% for 5-10L
    private static final BigDecimal TAX_RATE_HIGH = new BigDecimal("0.30");    // 30% for > 10L
    private static final BigDecimal LOW_TAX_THRESHOLD = new BigDecimal("500000").divide(new BigDecimal("12"), 2, RoundingMode.HALF_UP);
    private static final BigDecimal HIGH_TAX_THRESHOLD = new BigDecimal("1000000").divide(new BigDecimal("12"), 2, RoundingMode.HALF_UP);

    public PayrollResponse generatePayroll(PayrollRequest request) {
        Employee employee = employeeRepository.findById(request.getEmployeeId())
            .orElseThrow(() -> new RuntimeException("Employee not found"));

        if (employee.getStatus() != Employee.EmploymentStatus.APPROVED) {
            throw new RuntimeException("Payroll can only be generated for APPROVED employees. Current status: " + employee.getStatus());
        }

        if (payrollRepository.findByEmployeeIdAndPayMonthAndPayYear(
                request.getEmployeeId(), request.getPayMonth(), request.getPayYear()).isPresent()) {
            throw new RuntimeException("Payroll already generated for this employee for the given month/year");
        }

        Payroll payroll = new Payroll();
        payroll.setEmployee(employee);
        payroll.setPayMonth(request.getPayMonth());
        payroll.setPayYear(request.getPayYear());

        BigDecimal basicSalary = request.getBasicSalary() != null ?
            request.getBasicSalary() : employee.getBasicSalary();
        payroll.setBasicSalary(basicSalary);

        // Calculate HRA (40% of basic)
        BigDecimal hra = basicSalary.multiply(HRA_RATE).setScale(2, RoundingMode.HALF_UP);
        payroll.setHra(hra);

        // Set bonus and other allowances
        payroll.setBonus(request.getBonus() != null ? request.getBonus() : BigDecimal.ZERO);
        payroll.setOtherAllowances(request.getOtherAllowances() != null ? request.getOtherAllowances() : BigDecimal.ZERO);

        // Total Allowances
        BigDecimal totalAllowances = hra.add(payroll.getBonus()).add(payroll.getOtherAllowances());
        payroll.setTotalAllowances(totalAllowances);

        // Calculate PF Deduction (12% of basic)
        BigDecimal pf = basicSalary.multiply(PF_RATE).setScale(2, RoundingMode.HALF_UP);
        payroll.setPfDeduction(pf);

        // Calculate Tax
        BigDecimal tax = calculateTax(basicSalary);
        payroll.setTaxDeduction(tax);

        payroll.setOtherDeductions(request.getOtherDeductions() != null ? request.getOtherDeductions() : BigDecimal.ZERO);

        // Total Deductions
        BigDecimal totalDeductions = pf.add(tax).add(payroll.getOtherDeductions());
        payroll.setTotalDeductions(totalDeductions);

        // Net Salary = Basic + Total Allowances - Total Deductions
        BigDecimal netSalary = basicSalary.add(totalAllowances).subtract(totalDeductions)
            .setScale(2, RoundingMode.HALF_UP);
        payroll.setNetSalary(netSalary);

        payroll.setRemarks(request.getRemarks());
        payroll.setStatus(Payroll.PayrollStatus.PENDING);

        payroll = payrollRepository.save(payroll);
        return PayrollResponse.fromEntity(payroll);
    }

    private BigDecimal calculateTax(BigDecimal monthlySalary) {
        if (monthlySalary.compareTo(LOW_TAX_THRESHOLD) <= 0) {
            return monthlySalary.multiply(TAX_RATE_LOW).setScale(2, RoundingMode.HALF_UP);
        } else if (monthlySalary.compareTo(HIGH_TAX_THRESHOLD) <= 0) {
            return monthlySalary.multiply(TAX_RATE_MID).setScale(2, RoundingMode.HALF_UP);
        } else {
            return monthlySalary.multiply(TAX_RATE_HIGH).setScale(2, RoundingMode.HALF_UP);
        }
    }

    public PayrollResponse updatePayrollStatus(Long id, String status, String userRole) {
        Payroll payroll = payrollRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Payroll not found"));
        
        Payroll.PayrollStatus newStatus = Payroll.PayrollStatus.valueOf(status);

        // Security check
        if (newStatus == Payroll.PayrollStatus.APPROVED && !userRole.contains("ADMIN")) {
            throw new RuntimeException("Only Admins can approve payroll");
        }

        payroll.setStatus(newStatus);
        payroll = payrollRepository.save(payroll);
        return PayrollResponse.fromEntity(payroll);
    }

    public void exportPayslip(Long id, HttpServletResponse response) throws IOException {
        Payroll payroll = payrollRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Payroll not found"));
        payslipService.exportPayslipToPDF(payroll, response);
    }

    public void exportMonthlyPayroll(int month, int year, HttpServletResponse response) throws IOException {
        List<Payroll> payrolls = payrollRepository.findByPayMonthAndPayYear(month, year);
        payslipService.exportPayrollListToExcel(payrolls, response);
    }

    @Transactional(readOnly = true)
    public List<PayrollResponse> getEmployeePayrolls(Long employeeId) {
        return payrollRepository.findByEmployeeId(employeeId)
            .stream().map(PayrollResponse::fromEntity).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<PayrollResponse> getMonthlyPayroll(int month, int year, String search, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Payroll> payrollPage;
        if (search != null && !search.isEmpty()) {
            payrollPage = payrollRepository.findByMonthYearAndSearch(month, year, search, pageable);
        } else {
            payrollPage = payrollRepository.findByPayMonthAndPayYear(month, year, pageable);
        }
        return payrollPage.map(PayrollResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public DashboardStats getDashboardStats(int month, int year) {
        DashboardStats stats = new DashboardStats();
        stats.setTotalEmployees(employeeRepository.count());
        stats.setActiveEmployees(employeeRepository.countByStatus(Employee.EmploymentStatus.APPROVED));

        BigDecimal monthlyPayroll = payrollRepository.sumNetSalaryByMonthAndYear(month, year);
        stats.setTotalPayrollThisMonth(monthlyPayroll != null ? monthlyPayroll : BigDecimal.ZERO);

        BigDecimal yearlyPayroll = payrollRepository.sumNetSalaryByYear(year);
        stats.setTotalPayrollThisYear(yearlyPayroll != null ? yearlyPayroll : BigDecimal.ZERO);

        stats.setPayrollsProcessedThisMonth(payrollRepository.countByMonthAndYear(month, year));

        // Monthly trend
        List<Object[]> monthlyData = payrollRepository.getMonthlySummaryByYear(year);
        List<MonthlySummary> trend = monthlyData.stream().map(row -> {
            int m = ((Number) row[0]).intValue();
            BigDecimal amount = row[1] != null ? (BigDecimal) row[1] : BigDecimal.ZERO;
            return new MonthlySummary(m, Month.of(m).name(), amount);
        }).collect(Collectors.toList());
        stats.setMonthlyTrend(trend);

        return stats;
    }

    @Transactional(readOnly = true)
    public PayrollResponse getPayrollById(Long id) {
        return payrollRepository.findById(id)
            .map(PayrollResponse::fromEntity)
            .orElseThrow(() -> new RuntimeException("Payroll not found"));
    }

    @Transactional(readOnly = true)
    public List<PayrollResponse> getPendingPayrolls() {
        return payrollRepository.findByStatus(Payroll.PayrollStatus.PENDING)
            .stream().map(PayrollResponse::fromEntity).collect(Collectors.toList());
    }

    public void deletePayroll(Long id) {
        payrollRepository.deleteById(id);
    }
}
