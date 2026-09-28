package com.college.club_management.contoller;

import java.util.List;

import com.college.club_management.entity.Registration;
import com.college.club_management.service.RegistrationService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/registrations")
public class RegistrationController {

    private final RegistrationService registrationService;

    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @PostMapping
    public ResponseEntity<Registration> createRegistration(@Valid @RequestBody Registration registration) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(registrationService.createRegistration(registration));
    }

    @GetMapping
    public List<Registration> getRegistrations() {
        return registrationService.getRegistrations();
    }

    @GetMapping("/{id}")
    public Registration getRegistration(@PathVariable Long id) {
        return registrationService.getRegistration(id);
    }

    @PutMapping("/{id}")
    public Registration updateRegistration(@PathVariable Long id,
            @Valid @RequestBody Registration registration) {
        return registrationService.updateRegistration(id, registration);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRegistration(@PathVariable Long id) {
        registrationService.deleteRegistration(id);
        return ResponseEntity.noContent().build();
    }
}