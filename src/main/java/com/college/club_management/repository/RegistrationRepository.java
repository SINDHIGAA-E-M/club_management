package com.college.club_management.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.college.club_management.entity.Registration;

public interface RegistrationRepository extends JpaRepository<Registration, Long> {
}