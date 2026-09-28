package com.college.club_management.entity;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "registrations")
public class Registration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long registrationId;

    @NotNull(message = "Registration date is required")
    private LocalDate registrationDate = LocalDate.now();

    @NotBlank(message = "Status is required")
    private String status;

    @JsonIgnore
    @NotNull(message = "Member ID is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @JsonIgnore
    @NotNull(message = "Event ID is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    public Registration() {
    }

    public Long getRegistrationId() {
        return registrationId;
    }

    public void setRegistrationId(Long registrationId) {
        this.registrationId = registrationId;
    }

    @JsonProperty("memberId")
    public Long getMemberId() {
        return member == null ? null : member.getMemberId();
    }

    @JsonProperty("memberId")
    public void setMemberId(Long memberId) {
        if (memberId == null) {
            this.member = null;
        } else {
            this.member = new Member();
            this.member.setMemberId(memberId);
        }
    }

    @JsonProperty("eventId")
    public Long getEventId() {
        return event == null ? null : event.getEventId();
    }

    @JsonProperty("eventId")
    public void setEventId(Long eventId) {
        if (eventId == null) {
            this.event = null;
        } else {
            this.event = new Event();
            this.event.setEventId(eventId);
        }
    }

    public void setMember(Member member) {
        this.member = member;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public LocalDate getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDate registrationDate) {
        this.registrationDate = registrationDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}