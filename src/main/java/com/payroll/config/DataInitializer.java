package com.payroll.config;

import com.payroll.entity.Announcement;
import com.payroll.entity.CompanySettings;
import com.payroll.entity.User;
import com.payroll.repository.AnnouncementRepository;
import com.payroll.repository.CompanySettingsRepository;
import com.payroll.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private AnnouncementRepository announcementRepository;

    @Autowired
    private CompanySettingsRepository companySettingsRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // 1. Ensure Admin User exists
        User admin = userRepository.findByUsername("admin").orElse(null);
        if (admin == null) {
            admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(User.Role.ADMIN);
            admin.setFullName("System Administrator");
            admin.setEmail("admin@antigravity.io");
            admin = userRepository.save(admin);
        }

        // 2. Create demo announcements if none exist
        if (announcementRepository.count() == 0) {
            saveAnnouncement("Welcome to the New Payroll Portal!", 
                "We are excited to launch our new internal employee management and payroll system. Please explore the dashboard and update your profile.", 
                "GENERAL", admin);
            
            saveAnnouncement("Holiday Calendar 2026", 
                "The annual holiday list for 2026 has been uploaded to the resources section. We have 12 paid public holidays this year.", 
                "POLICY", admin);
                
            saveAnnouncement("Quarterly Town Hall", 
                "Our Q1 Town Hall is scheduled for next Friday at 4:30 PM. Join us to discuss our progress and future roadmap.", 
                "EVENT", admin);
        }

        // 3. Create default company settings if none exist
        if (companySettingsRepository.count() == 0) {
            CompanySettings settings = new CompanySettings();
            settings.setCompanyName("Antigravity Tech Solutions");
            settings.setCompanyAddress("123 Innovation Drive, Cyber City, Bangalore - 560001");
            settings.setContactEmail("ops@antigravity.io");
            settings.setContactPhone("+91 80 4992 0000");
            settings.setWebsite("https://antigravity.io");
            settings.setTaxId("GST29AAA1234F1Z0");
            settings.setRegistrationNumber("U72200KA2024PTC123456");
            settings.setCurrency("INR");
            settings.setCurrencySymbol("₹");
            settings.setLogoUrl(""); 
            companySettingsRepository.save(settings);
        }
    }

    private void saveAnnouncement(String title, String content, String type, User author) {
        Announcement ann = new Announcement();
        ann.setTitle(title);
        ann.setContent(content);
        ann.setType(type);
        ann.setAuthor(author);
        ann.setCreatedAt(LocalDateTime.now());
        announcementRepository.save(ann);
    }
}
