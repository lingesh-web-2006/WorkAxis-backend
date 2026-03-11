package com.payroll.repository;

import com.payroll.entity.Payroll;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface PayrollRepository extends JpaRepository<Payroll, Long> {

    List<Payroll> findByEmployeeId(Long employeeId);

    Optional<Payroll> findByEmployeeIdAndPayMonthAndPayYear(Long employeeId, Integer payMonth, Integer payYear);

    Page<Payroll> findByPayMonthAndPayYear(Integer payMonth, Integer payYear, Pageable pageable);
    
    List<Payroll> findByPayMonthAndPayYear(Integer payMonth, Integer payYear);

    @Query("SELECT SUM(p.netSalary) FROM Payroll p WHERE p.payMonth = :month AND p.payYear = :year")
    BigDecimal sumNetSalaryByMonthAndYear(@Param("month") Integer month, @Param("year") Integer year);

    @Query("SELECT SUM(p.netSalary) FROM Payroll p WHERE p.payYear = :year")
    BigDecimal sumNetSalaryByYear(@Param("year") Integer year);

    @Query("SELECT COUNT(p) FROM Payroll p WHERE p.payMonth = :month AND p.payYear = :year")
    long countByMonthAndYear(@Param("month") Integer month, @Param("year") Integer year);

    @Query("SELECT p.payMonth, SUM(p.netSalary) FROM Payroll p WHERE p.payYear = :year GROUP BY p.payMonth ORDER BY p.payMonth")
    List<Object[]> getMonthlySummaryByYear(@Param("year") Integer year);

    @Query("SELECT p FROM Payroll p WHERE p.payMonth = :month AND p.payYear = :year " +
           "AND (:search IS NULL OR LOWER(p.employee.name) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Payroll> findByMonthYearAndSearch(@Param("month") Integer month, @Param("year") Integer year,
                                            @Param("search") String search, Pageable pageable);

    List<Payroll> findByStatus(Payroll.PayrollStatus status);
}
