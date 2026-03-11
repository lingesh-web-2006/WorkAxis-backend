package com.payroll;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PayrollSystemApplication implements org.springframework.boot.CommandLineRunner {

    @org.springframework.beans.factory.annotation.Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    public static void main(String[] args) {
        SpringApplication.run(PayrollSystemApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        try {
            jdbcTemplate.execute("ALTER TABLE payroll MODIFY COLUMN status VARCHAR(20)");
            System.out.println("Payroll table status column updated to VARCHAR(20)");
        } catch (Exception e) {
            System.err.println("Note: Could not alter payroll table (might already be correct): " + e.getMessage());
        }
    }
}
