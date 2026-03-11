package com.payroll.controller;

import com.payroll.entity.CompanySettings;
import com.payroll.repository.CompanySettingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/company")
public class CompanyController {

    @Autowired
    private CompanySettingsRepository companySettingsRepository;

    @GetMapping("/settings")
    public ResponseEntity<CompanySettings> getSettings() {
        return ResponseEntity.ok(companySettingsRepository.findFirstByOrderByIdAsc()
                .orElse(new CompanySettings()));
    }

    @PutMapping("/settings")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CompanySettings> updateSettings(@RequestBody CompanySettings settings) {
        CompanySettings existing = companySettingsRepository.findFirstByOrderByIdAsc()
                .orElse(new CompanySettings());
        
        settings.setId(existing.getId());
        return ResponseEntity.ok(companySettingsRepository.save(settings));
    }
}
