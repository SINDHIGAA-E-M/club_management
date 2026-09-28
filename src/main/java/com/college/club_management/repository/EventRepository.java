package com.college.club_management.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.college.club_management.entity.Event;

public interface EventRepository extends JpaRepository<Event, Long> {
}