package com.college.club_management.service;

import java.util.List;

import com.college.club_management.entity.Member;
import com.college.club_management.repository.ClubRepository;
import com.college.club_management.repository.MemberRepository;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final ClubRepository clubRepository;

    public MemberService(MemberRepository memberRepository, ClubRepository clubRepository) {
        this.memberRepository = memberRepository;
        this.clubRepository = clubRepository;
    }

    public Member createMember(Member member) {
        validateClub(member.getClubId());
        member.setClub(clubRepository.getReferenceById(member.getClubId()));
        return memberRepository.save(member);
    }

    public List<Member> getMembers() {
        return memberRepository.findAll();
    }

    public Member getMember(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Member not found with id " + id));
    }

    public Member updateMember(Long id, Member updatedMember) {
        validateClub(updatedMember.getClubId());
        Member member = getMember(id);
        member.setClub(clubRepository.getReferenceById(updatedMember.getClubId()));
        member.setName(updatedMember.getName());
        member.setEmail(updatedMember.getEmail());
        member.setDepartment(updatedMember.getDepartment());
        member.setYear(updatedMember.getYear());
        member.setPhoneNumber(updatedMember.getPhoneNumber());
        return memberRepository.save(member);
    }

    public void deleteMember(Long id) {
        memberRepository.delete(getMember(id));
    }

    private void validateClub(Long clubId) {
        if (!clubRepository.existsById(clubId)) {
            throw new IllegalArgumentException("Club not found with id " + clubId);
        }
    }
}