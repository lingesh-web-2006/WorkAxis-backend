package com.payroll.controller;

import com.payroll.dto.AuthDTOs.*;
import com.payroll.entity.User;
import com.payroll.repository.UserRepository;
import com.payroll.security.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private JwtUtils jwtUtils;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        User user = userRepository.findByUsername(loginRequest.getUsername())
            .orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok(new LoginResponse(
            jwt, user.getId(), user.getUsername(),
            user.getEmail(), user.getFullName(), user.getRole().name()
        ));
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest registerRequest) {
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Username is already taken!"));
        }

        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Email is already in use!"));
        }

        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setPassword(encoder.encode(registerRequest.getPassword()));
        user.setEmail(registerRequest.getEmail());
        user.setFullName(registerRequest.getFullName());
        user.setRole(registerRequest.getRole() != null ? registerRequest.getRole() : User.Role.EMPLOYEE);

        userRepository.save(user);
        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }

    @PostMapping("/init")
    public ResponseEntity<?> initAdmin() {
        StringBuilder message = new StringBuilder();

        // Ensure Admin exists
        // First try by username
        User admin = userRepository.findByUsername("admin")
            .orElseGet(() -> userRepository.findByEmail("admin@payroll.com").orElse(new User()));
            
        boolean isAdminNew = admin.getId() == null;
        admin.setUsername("admin");
        admin.setPassword(encoder.encode("admin123"));
        admin.setEmail("admin@payroll.com");
        admin.setFullName("System Administrator");
        admin.setRole(User.Role.ADMIN);
        userRepository.save(admin);
        message.append(isAdminNew ? "Admin created. " : "Admin updated. ");

        // Ensure HR (username: employee) exists
        // First try by username
        User hr = userRepository.findByUsername("employee")
            .orElseGet(() -> userRepository.findByEmail("hr@payroll.com").orElse(new User()));

        boolean isHrNew = hr.getId() == null;
        hr.setUsername("employee");
        hr.setPassword(encoder.encode("employee123"));
        hr.setEmail("hr@payroll.com");
        hr.setFullName("HR Manager");
        hr.setRole(User.Role.HR);
        userRepository.save(hr);
        message.append(isHrNew ? "HR (employee) created." : "HR (employee) updated.");

        return ResponseEntity.ok(new MessageResponse(message.toString()));
    }
}
