package com.college.club_management.service;

import java.util.List;

import com.college.club_management.entity.Registration;
import com.college.club_management.repository.EventRepository;
import com.college.club_management.repository.MemberRepository;
import com.college.club_management.repository.RegistrationRepository;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class RegistrationService {

    private final RegistrationRepository registrationRepository;
    private final MemberRepository memberRepository;
    private final EventRepository eventRepository;

    public RegistrationService(RegistrationRepository registrationRepository,
            MemberRepository memberRepository, EventRepository eventRepository) {
        this.registrationRepository = registrationRepository;
        this.memberRepository = memberRepository;
        this.eventRepository = eventRepository;
    }

    public Registration createRegistration(Registration registration) {
        validateReferences(registration);
        registration.setMember(memberRepository.getReferenceById(registration.getMemberId()));
        registration.setEvent(eventRepository.getReferenceById(registration.getEventId()));
        return registrationRepository.save(registration);
    }

    public List<Registration> getRegistrations() {
        return registrationRepository.findAll();
    }

    public Registration getRegistration(Long id) {
        return registrationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Registration not found with id " + id));
    }

    public Registration updateRegistration(Long id, Registration updatedRegistration) {
        validateReferences(updatedRegistration);
        Registration registration = getRegistration(id);
        registration.setMember(memberRepository.getReferenceById(updatedRegistration.getMemberId()));
        registration.setEvent(eventRepository.getReferenceById(updatedRegistration.getEventId()));
        registration.setRegistrationDate(updatedRegistration.getRegistrationDate());
        registration.setStatus(updatedRegistration.getStatus());
        return registrationRepository.save(registration);
    }

    public void deleteRegistration(Long id) {
        registrationRepository.delete(getRegistration(id));
    }

    private void validateReferences(Registration registration) {
        if (!memberRepository.existsById(registration.getMemberId())) {
            throw new IllegalArgumentException("Member not found with id " + registration.getMemberId());
        }
        if (!eventRepository.existsById(registration.getEventId())) {
            throw new IllegalArgumentException("Event not found with id " + registration.getEventId());
        }
    }
}