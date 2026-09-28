/*package com.college.club_management.contoller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/clubs")
public class ClubController {

    @GetMapping
    public String getClubs() {
        return "College Club Management System is working!";
    }
}*/
package com.college.club_management.contoller;

import java.util.List;

import com.college.club_management.entity.Club;
import com.college.club_management.service.ClubService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/clubs")
public class ClubController {

    private final ClubService clubService;

    public ClubController(ClubService clubService) {
        this.clubService = clubService;
    }

    @PostMapping
    public ResponseEntity<Club> createClub(@Valid @RequestBody Club club) {
        return ResponseEntity.status(HttpStatus.CREATED).body(clubService.createClub(club));
    }

    @GetMapping
    public List<Club> getClubs() {
        return clubService.getClubs();
    }

    @GetMapping("/{id}")
    public Club getClub(@PathVariable Long id) {
        return clubService.getClub(id);
    }

    @PutMapping("/{id}")
    public Club updateClub(@PathVariable Long id, @Valid @RequestBody Club club) {
        return clubService.updateClub(id, club);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClub(@PathVariable Long id) {
        clubService.deleteClub(id);
        return ResponseEntity.noContent().build();
    }
}