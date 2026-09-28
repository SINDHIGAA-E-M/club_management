package com.college.club_management.service;

import java.util.List;

import com.college.club_management.entity.Event;
import com.college.club_management.repository.ClubRepository;
import com.college.club_management.repository.EventRepository;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final ClubRepository clubRepository;

    public EventService(EventRepository eventRepository, ClubRepository clubRepository) {
        this.eventRepository = eventRepository;
        this.clubRepository = clubRepository;
    }

    public Event createEvent(Event event) {
        validateClub(event.getClubId());
        event.setClub(clubRepository.getReferenceById(event.getClubId()));
        return eventRepository.save(event);
    }

    public List<Event> getEvents() {
        return eventRepository.findAll();
    }

    public Event getEvent(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Event not found with id " + id));
    }

    public Event updateEvent(Long id, Event updatedEvent) {
        validateClub(updatedEvent.getClubId());
        Event event = getEvent(id);
        event.setClub(clubRepository.getReferenceById(updatedEvent.getClubId()));
        event.setEventName(updatedEvent.getEventName());
        event.setDescription(updatedEvent.getDescription());
        event.setEventDate(updatedEvent.getEventDate());
        event.setVenue(updatedEvent.getVenue());
        return eventRepository.save(event);
    }

    public void deleteEvent(Long id) {
        eventRepository.delete(getEvent(id));
    }

    private void validateClub(Long clubId) {
        if (!clubRepository.existsById(clubId)) {
            throw new IllegalArgumentException("Club not found with id " + clubId);
        }
    }
}