package com.college.club_management.service;

import java.util.List;

import com.college.club_management.entity.Club;
import com.college.club_management.repository.ClubRepository;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class ClubService {

    private final ClubRepository clubRepository;

    public ClubService(ClubRepository clubRepository) {
        this.clubRepository = clubRepository;
    }

    public Club createClub(Club club) {
        return clubRepository.save(club);
    }

    public List<Club> getClubs() {
        return clubRepository.findAll();
    }

    public Club getClub(Long id) {
        return clubRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Club not found with id " + id));
    }

    public Club updateClub(Long id, Club updatedClub) {
        Club club = getClub(id);
        club.setClubName(updatedClub.getClubName());
        club.setDescription(updatedClub.getDescription());
        club.setCoordinatorName(updatedClub.getCoordinatorName());
        club.setCategory(updatedClub.getCategory());
        club.setContactEmail(updatedClub.getContactEmail());
        return clubRepository.save(club);
    }

    public void deleteClub(Long id) {
        clubRepository.delete(getClub(id));
    }
}