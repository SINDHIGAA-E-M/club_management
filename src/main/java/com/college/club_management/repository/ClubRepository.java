package com.college.club_management.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.college.club_management.entity.Club;

public interface ClubRepository extends JpaRepository<Club, Long> {
}