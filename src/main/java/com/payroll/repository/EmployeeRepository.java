package com.payroll.repository;

import com.payroll.entity.Employee;
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
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Optional<Employee> findByEmail(String email);

    java.util.List<Employee> findByStatus(Employee.EmploymentStatus status);

    boolean existsByEmail(String email);

    @Query("SELECT e FROM Employee e WHERE " +
           "(:search IS NULL OR LOWER(e.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(e.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(e.department) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(e.position) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Employee> findBySearchTerm(@Param("search") String search, Pageable pageable);

    @Query("SELECT COUNT(e) FROM Employee e WHERE e.status = :status")
    long countByStatus(@Param("status") Employee.EmploymentStatus status);

    @Query("SELECT SUM(e.basicSalary) FROM Employee e WHERE e.status = :status")
    BigDecimal sumBasicSalaryByStatus(@Param("status") Employee.EmploymentStatus status);

    @Query("SELECT DISTINCT e.department FROM Employee e ORDER BY e.department")
    java.util.List<String> findAllDepartments();

    @Query("SELECT e.department as department, COUNT(e) as count FROM Employee e GROUP BY e.department")
    List<Object[]> findDepartmentDistribution();

    @Query("SELECT e.status as status, COUNT(e) as count FROM Employee e GROUP BY e.status")
    List<Object[]> findStatusDistribution();

    @Query("SELECT e.department as department, COUNT(e) as count, SUM(e.basicSalary) as totalSalary FROM Employee e GROUP BY e.department")
    List<Object[]> findDepartmentSalaryStats();
}
