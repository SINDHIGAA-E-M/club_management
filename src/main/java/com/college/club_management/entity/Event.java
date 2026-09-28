package com.college.club_management.entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "club_events")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long eventId;

    @NotBlank(message = "Event name is required")
    private String eventName;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Event date is required")
    private LocalDate eventDate;

    @NotBlank(message = "Venue is required")
    private String venue;

    @NotNull(message = "Club ID is required")
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_id", nullable = false)
    private Club club;

    @JsonIgnore
    @OneToMany(mappedBy = "event")
    private List<Registration> registrations = new ArrayList<>();

    public Event() {
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getEventDate() {
        return eventDate;
    }

    public void setEventDate(LocalDate eventDate) {
        this.eventDate = eventDate;
    }

    public String getVenue() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    @JsonProperty("clubId")
    public Long getClubId() {
        return club == null ? null : club.getClubId();
    }

    @JsonProperty("clubId")
    public void setClubId(Long clubId) {
        if (clubId == null) {
            this.club = null;
        } else {
            this.club = new Club();
            this.club.setClubId(clubId);
        }
    }

    public void setClub(Club club) {
        this.club = club;
    }
}