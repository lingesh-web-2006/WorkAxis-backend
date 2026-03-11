package com.payroll.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "company_settings")
@Data
public class CompanySettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String companyName;

    private String companyAddress;
    private String contactEmail;
    private String contactPhone;
    private String website;
    
    // Tax details
    private String taxId;
    private String registrationNumber;
    
    // Currency & Localisation
    private String currency = "INR";
    private String currencySymbol = "₹";
    
    // Branding
    private String logoUrl;
}
