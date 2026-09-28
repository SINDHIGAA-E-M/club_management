package com.college.club_management.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.college.club_management.entity.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {
}