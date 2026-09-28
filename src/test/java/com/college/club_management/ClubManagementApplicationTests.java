package com.college.club_management;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.college.club_management.repository.ClubRepository;
import com.college.club_management.repository.EventRepository;
import com.college.club_management.repository.MemberRepository;
import com.college.club_management.repository.RegistrationRepository;

@SpringBootTest(properties = {
		"spring.datasource.url=jdbc:h2:mem:club_management_test;DB_CLOSE_DELAY=-1",
		"spring.datasource.driver-class-name=org.h2.Driver",
		"spring.datasource.username=sa",
		"spring.datasource.password="
})
class ClubManagementApplicationTests {

	@Autowired
	private WebApplicationContext applicationContext;

	@Autowired
	private ClubRepository clubRepository;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private EventRepository eventRepository;

	@Autowired
	private RegistrationRepository registrationRepository;

	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.webAppContextSetup(applicationContext).build();
		registrationRepository.deleteAll();
		eventRepository.deleteAll();
		memberRepository.deleteAll();
		clubRepository.deleteAll();
	}

	@Test
	void contextLoads() {
	}

	@Test
	void crudWorksAcrossAllModules() throws Exception {
		long clubId = create("/api/clubs", """
				{"clubName":"Robotics Club","description":"Build and learn","coordinatorName":"Asha Rao","category":"Technology","contactEmail":"robotics@college.edu"}
				""", "clubId");
		mockMvc.perform(get("/api/clubs/{id}", clubId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.clubName").value("Robotics Club"));
		mockMvc.perform(put("/api/clubs/{id}", clubId).contentType(MediaType.APPLICATION_JSON).content("""
				{"clubName":"Robotics Society","description":"Build and learn","coordinatorName":"Asha Rao","category":"Technology","contactEmail":"robotics@college.edu"}
				"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.clubName").value("Robotics Society"));

		long memberId = create("/api/members", """
				{"name":"Sam Lee","email":"sam@college.edu","department":"Computer Science","year":2,"phoneNumber":"555-0100","clubId":%d}
				""".formatted(clubId), "memberId");
		mockMvc.perform(get("/api/members"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].clubId").value(clubId));
		mockMvc.perform(put("/api/members/{id}", memberId).contentType(MediaType.APPLICATION_JSON).content("""
				{"name":"Sam Lee","email":"sam@college.edu","department":"Computer Science","year":3,"phoneNumber":"555-0100","clubId":%d}
				""".formatted(clubId)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.year").value(3));

		long eventId = create("/api/events", """
				{"eventName":"Build Night","description":"An evening workshop","eventDate":"2026-11-12","venue":"Lab 2","clubId":%d}
				""".formatted(clubId), "eventId");
		mockMvc.perform(get("/api/events/{id}", eventId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.clubId").value(clubId));

		long registrationId = create("/api/registrations", """
				{"memberId":%d,"eventId":%d,"registrationDate":"2026-09-28","status":"Pending"}
				""".formatted(memberId, eventId), "registrationId");
		mockMvc.perform(put("/api/registrations/{id}", registrationId)
				.contentType(MediaType.APPLICATION_JSON).content("""
						{"memberId":%d,"eventId":%d,"registrationDate":"2026-09-28","status":"Approved"}
						""".formatted(memberId, eventId)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("Approved"));
		mockMvc.perform(get("/api/registrations"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].memberId").value(memberId));

		mockMvc.perform(delete("/api/registrations/{id}", registrationId)).andExpect(status().isNoContent());
		mockMvc.perform(delete("/api/events/{id}", eventId)).andExpect(status().isNoContent());
		mockMvc.perform(delete("/api/members/{id}", memberId)).andExpect(status().isNoContent());
		mockMvc.perform(delete("/api/clubs/{id}", clubId)).andExpect(status().isNoContent());
		mockMvc.perform(get("/api/clubs/{id}", clubId)).andExpect(status().isNotFound());
	}

	@Test
	void invalidAndMissingRequestsReturnJsonErrors() throws Exception {
		mockMvc.perform(post("/api/clubs").contentType(MediaType.APPLICATION_JSON).content("""
				{"clubName":"","description":"","coordinatorName":"","category":"","contactEmail":"not-an-email"}
				"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error").value("Validation failed"));
		mockMvc.perform(get("/api/events/{id}", 9999))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message").value("Event not found with id 9999"));
	}

	private long create(String path, String body, String idField) throws Exception {
		String response = mockMvc.perform(post(path).contentType(MediaType.APPLICATION_JSON).content(body))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();
		Matcher idMatcher = Pattern.compile("\\\"" + idField + "\\\":(\\d+)").matcher(response);
		if (!idMatcher.find()) {
			throw new AssertionError("Response did not contain " + idField + ": " + response);
		}
		return Long.parseLong(idMatcher.group(1));
	}

}
