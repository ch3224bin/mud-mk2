package com.jefflife.mudmk2.game.adapter.in;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jefflife.mudmk2.game.application.service.model.response.RoomResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "test@example.com", roles = "USER")
public class RoomControllerSystemTest {

    private static final String BASE_URL = "/api/v1/rooms";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createRoom_shouldCreateRoomAndReturnCreatedResponse() throws Exception {
        // Given
        long areaId = 1L;
        String summary = "Test Room";
        String description = "This is a test room description";
        String requestJson = createRoomJson(areaId, summary, description);

        // When
        MvcResult result = mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andReturn();

        // Then
        String responseJson = result.getResponse().getContentAsString();
        RoomResponse response = objectMapper.readValue(responseJson, RoomResponse.class);

        assertThat(response.id()).isNotNull();
        assertRoomEquals(response, areaId, summary, description);

        // Verify Location header
        String locationHeader = result.getResponse().getHeader("Location");
        assertThat(locationHeader).isEqualTo(BASE_URL + "/" + response.id());
    }

    private String createRoomJson(long areaId, String summary, String description) {
        return "{"
                + "\"areaId\": " + areaId + ","
                + "\"summary\": \"" + summary + "\","
                + "\"description\": \"" + description + "\""
                + "}";
    }

    private void assertRoomEquals(RoomResponse room, long expectedAreaId, String expectedSummary, String expectedDescription) {
        assertThat(room).isNotNull();
        assertThat(room.areaId()).isEqualTo(expectedAreaId);
        assertThat(room.summary()).isEqualTo(expectedSummary);
        assertThat(room.description()).isEqualTo(expectedDescription);
    }

    @Test
    @Transactional
    void updateRoom_shouldUpdateRoomAndReturnOkResponse() throws Exception {
        // Given
        // First create a room
        long areaId = 1L;
        String originalSummary = "Original Room";
        String originalDescription = "This is the original room description";
        String createRequestJson = createRoomJson(areaId, originalSummary, originalDescription);

        MvcResult createResult = mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createRequestJson))
                .andExpect(status().isCreated())
                .andReturn();

        String createResponseJson = createResult.getResponse().getContentAsString();
        RoomResponse createResponse = objectMapper.readValue(createResponseJson, RoomResponse.class);
        long roomId = createResponse.id();

        // Now update the room
        String updatedSummary = "Updated Room";
        String updatedDescription = "This is the updated room description";
        String updateRequestJson = updateRoomJson(updatedSummary, updatedDescription);

        // When
        MvcResult updateResult = mockMvc.perform(patch(BASE_URL + "/" + roomId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateRequestJson))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        String updateResponseJson = updateResult.getResponse().getContentAsString();
        RoomResponse updateResponse = objectMapper.readValue(updateResponseJson, RoomResponse.class);

        assertThat(updateResponse.id()).isEqualTo(roomId);
        assertThat(updateResponse.areaId()).isEqualTo(areaId);
        assertThat(updateResponse.summary()).isEqualTo(updatedSummary);
        assertThat(updateResponse.description()).isEqualTo(updatedDescription);
    }

    private String updateRoomJson(String summary, String description) {
        return "{"
                + "\"summary\": \"" + summary + "\","
                + "\"description\": \"" + description + "\""
                + "}";
    }

    @Test
    @Transactional
    void getRoom_shouldReturnRoomAndOkResponse() throws Exception {
        // Given
        // First create a room
        long areaId = 1L;
        String summary = "Test Room for Get";
        String description = "This is a test room for the get method";
        String createRequestJson = createRoomJson(areaId, summary, description);

        MvcResult createResult = mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createRequestJson))
                .andExpect(status().isCreated())
                .andReturn();

        String createResponseJson = createResult.getResponse().getContentAsString();
        RoomResponse createResponse = objectMapper.readValue(createResponseJson, RoomResponse.class);
        long roomId = createResponse.id();

        // When
        MvcResult getResult = mockMvc.perform(get(BASE_URL + "/" + roomId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        String getResponseJson = getResult.getResponse().getContentAsString();
        RoomResponse getResponse = objectMapper.readValue(getResponseJson, RoomResponse.class);

        assertThat(getResponse.id()).isEqualTo(roomId);
        assertRoomEquals(getResponse, areaId, summary, description);
    }

    @Test
    @Transactional
    void getRooms_shouldReturnPagedRoomsAndOkResponse() throws Exception {
        // Given
        // Create multiple rooms
        long areaId = 1L;

        // Room 1 - Use unique identifiers to ensure we can find them
        String summary1 = "Test Room for Pagination 1 " + System.currentTimeMillis();
        String description1 = "This is test room 1 for pagination test";
        String createRequestJson1 = createRoomJson(areaId, summary1, description1);

        MvcResult createResult1 = mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createRequestJson1))
                .andExpect(status().isCreated())
                .andReturn();

        String createResponseJson1 = createResult1.getResponse().getContentAsString();
        RoomResponse createResponse1 = objectMapper.readValue(createResponseJson1, RoomResponse.class);

        // Room 2 - Use unique identifiers to ensure we can find them
        String summary2 = "Test Room for Pagination 2 " + System.currentTimeMillis();
        String description2 = "This is test room 2 for pagination test";
        String createRequestJson2 = createRoomJson(areaId, summary2, description2);

        MvcResult createResult2 = mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createRequestJson2))
                .andExpect(status().isCreated())
                .andReturn();

        String createResponseJson2 = createResult2.getResponse().getContentAsString();
        RoomResponse createResponse2 = objectMapper.readValue(createResponseJson2, RoomResponse.class);

        // When
        // Request with a large page size to ensure we get all rooms
        MvcResult getResult = mockMvc.perform(get(BASE_URL)
                        .param("page", "0")
                        .param("size", "100") // Use a larger size to ensure we get all rooms
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        String getResponseJson = getResult.getResponse().getContentAsString();

        // Parse the paginated response
        Map<String, Object> pageResponse = objectMapper.readValue(getResponseJson, new TypeReference<Map<String, Object>>() {});

        // Verify pagination information
        assertThat(pageResponse.get("number")).isEqualTo(0); // Page number
        assertThat((Integer) pageResponse.get("size")).isGreaterThan(0); // Page size should be positive

        // Verify content contains our created rooms
        List<Map<String, Object>> content = (List<Map<String, Object>>) pageResponse.get("content");
        assertThat(content).isNotEmpty(); // Content should not be empty

        // Find our created rooms in the content by ID
        boolean foundRoom1 = false;
        boolean foundRoom2 = false;

        for (Map<String, Object> room : content) {
            long roomId = ((Number) room.get("id")).longValue();

            if (roomId == createResponse1.id()) {
                assertThat(room.get("summary")).isEqualTo(summary1);
                assertThat(room.get("description")).isEqualTo(description1);
                foundRoom1 = true;
            } else if (roomId == createResponse2.id()) {
                assertThat(room.get("summary")).isEqualTo(summary2);
                assertThat(room.get("description")).isEqualTo(description2);
                foundRoom2 = true;
            }

            // If we found both rooms, we can break early
            if (foundRoom1 && foundRoom2) {
                break;
            }
        }

        // Verify both rooms were found in the response
        assertThat(foundRoom1).withFailMessage("Room 1 (ID: %d) was not found in the response", createResponse1.id()).isTrue();
        assertThat(foundRoom2).withFailMessage("Room 2 (ID: %d) was not found in the response", createResponse2.id()).isTrue();
    }

    @Test
    @Transactional
    void deleteRoom_shouldDeleteRoomAndReturnNoContent() throws Exception {
        // Given
        // First create a room
        long areaId = 1L;
        String summary = "Test Room for Delete";
        String description = "This is a test room for the delete method";
        String createRequestJson = createRoomJson(areaId, summary, description);

        MvcResult createResult = mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createRequestJson))
                .andExpect(status().isCreated())
                .andReturn();

        String createResponseJson = createResult.getResponse().getContentAsString();
        RoomResponse createResponse = objectMapper.readValue(createResponseJson, RoomResponse.class);
        long roomId = createResponse.id();

        // When
        deleteRoom(roomId);

        // Then
        // Verify that the room was actually deleted by checking it's not in the list of all rooms
        MvcResult getResult = mockMvc.perform(get(BASE_URL)
                        .param("page", "0")
                        .param("size", "100") // Use a larger size to ensure we get all rooms
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String getResponseJson = getResult.getResponse().getContentAsString();
        Map<String, Object> pageResponse = objectMapper.readValue(getResponseJson, new TypeReference<Map<String, Object>>() {});
        List<Map<String, Object>> content = (List<Map<String, Object>>) pageResponse.get("content");

        // Check that the deleted room is not in the list
        boolean roomExists = content.stream()
                .anyMatch(room -> ((Number) room.get("id")).longValue() == roomId);

        assertThat(roomExists).withFailMessage("Room (ID: %d) was found in the response but should have been deleted", roomId).isFalse();
    }

    private void deleteRoom(Long roomId) throws Exception {
        mockMvc.perform(delete(BASE_URL + "/" + roomId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }
}
