package com.payroll.service;

import com.payroll.dto.EmployeeDTOs.*;
import com.payroll.entity.Employee;
import com.payroll.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;

    public EmployeeResponse createEmployee(EmployeeRequest request) {
        if (employeeRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Employee with email already exists: " + request.getEmail());
        }
        Employee employee = new Employee();
        mapRequestToEntity(request, employee);
        employee = employeeRepository.save(employee);
        return EmployeeResponse.fromEntity(employee);
    }

    public EmployeeResponse updateEmployee(Long id, EmployeeRequest request) {
        Employee employee = employeeRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Employee not found with id: " + id));

        if (!employee.getEmail().equals(request.getEmail()) &&
            employeeRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already in use: " + request.getEmail());
        }
        mapRequestToEntity(request, employee);
        employee = employeeRepository.save(employee);
        return EmployeeResponse.fromEntity(employee);
    }

    public void deleteEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Employee not found with id: " + id));
        employee.setStatus(Employee.EmploymentStatus.TERMINATED);
        employeeRepository.save(employee);
    }

    @Transactional(readOnly = true)
    public EmployeeResponse getEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Employee not found with id: " + id));
        return EmployeeResponse.fromEntity(employee);
    }

    @Transactional(readOnly = true)
    public PagedEmployeeResponse getAllEmployees(int page, int size, String search, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ?
            Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Employee> employeePage;
        if (search != null && !search.isEmpty()) {
            employeePage = employeeRepository.findBySearchTerm(search, pageable);
        } else {
            employeePage = employeeRepository.findAll(pageable);
        }

        PagedEmployeeResponse response = new PagedEmployeeResponse();
        response.setContent(employeePage.getContent().stream()
            .map(EmployeeResponse::fromEntity).collect(Collectors.toList()));
        response.setPage(employeePage.getNumber());
        response.setSize(employeePage.getSize());
        response.setTotalElements(employeePage.getTotalElements());
        response.setTotalPages(employeePage.getTotalPages());
        response.setLast(employeePage.isLast());
        return response;
    }

    @Transactional(readOnly = true)
    public List<String> getAllDepartments() {
        return employeeRepository.findAllDepartments();
    }

    @Transactional(readOnly = true)
    public List<EmployeeResponse> getActiveEmployees() {
        return employeeRepository.findByStatus(Employee.EmploymentStatus.APPROVED).stream()
            .map(EmployeeResponse::fromEntity)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EmployeeResponse> getPendingEmployees() {
        return employeeRepository.findByStatus(Employee.EmploymentStatus.PENDING).stream()
            .map(EmployeeResponse::fromEntity)
            .collect(Collectors.toList());
    }

    public EmployeeResponse approveEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Employee not found"));
        employee.setStatus(Employee.EmploymentStatus.APPROVED);
        employee = employeeRepository.save(employee);
        return EmployeeResponse.fromEntity(employee);
    }

    public EmployeeResponse rejectEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Employee not found"));
        employee.setStatus(Employee.EmploymentStatus.REJECTED);
        employee = employeeRepository.save(employee);
        return EmployeeResponse.fromEntity(employee);
    }

    @Transactional(readOnly = true)
    public List<java.util.Map<String, Object>> getDepartmentDistribution() {
        return employeeRepository.findDepartmentDistribution().stream()
            .map(obj -> {
                java.util.Map<String, Object> map = new java.util.HashMap<>();
                map.put("name", obj[0]);
                map.put("value", obj[1]);
                return map;
            })
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<java.util.Map<String, Object>> getStatusDistribution() {
        return employeeRepository.findStatusDistribution().stream()
            .map(obj -> {
                java.util.Map<String, Object> map = new java.util.HashMap<>();
                map.put("name", obj[0].toString());
                map.put("value", obj[1]);
                return map;
            })
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<java.util.Map<String, Object>> getDepartmentSalaryStats() {
        return employeeRepository.findDepartmentSalaryStats().stream()
            .map(obj -> {
                java.util.Map<String, Object> map = new java.util.HashMap<>();
                map.put("name", obj[0]);
                map.put("employees", obj[1]);
                map.put("totalSalary", obj[2]);
                return map;
            })
            .collect(Collectors.toList());
    }

    private void mapRequestToEntity(EmployeeRequest request, Employee employee) {
        employee.setName(request.getName());
        employee.setEmail(request.getEmail());
        employee.setDepartment(request.getDepartment());
        employee.setPosition(request.getPosition());
        employee.setJoiningDate(request.getJoiningDate());
        employee.setBasicSalary(request.getBasicSalary());
        employee.setPhone(request.getPhone());
        employee.setAddress(request.getAddress());
        if (request.getStatus() != null) {
            employee.setStatus(request.getStatus());
        }
    }
}
