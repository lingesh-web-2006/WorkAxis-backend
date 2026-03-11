package com.payroll.controller;

import com.payroll.entity.Announcement;
import com.payroll.entity.User;
import com.payroll.repository.AnnouncementRepository;
import com.payroll.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/announcements")
public class AnnouncementController {

    @Autowired
    private AnnouncementRepository announcementRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<Announcement>> getAll() {
        return ResponseEntity.ok(announcementRepository.findAll());
    }

    @GetMapping("/latest")
    public ResponseEntity<List<Announcement>> getLatest() {
        return ResponseEntity.ok(announcementRepository.findTop5ByOrderByCreatedAtDesc());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Announcement> create(@RequestBody Announcement announcement) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username).orElse(null);
        announcement.setAuthor(user);
        return ResponseEntity.ok(announcementRepository.save(announcement));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> delete(@PathVariable(name = "id") Long id) {
        announcementRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
