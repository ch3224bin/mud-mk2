package com.jefflife.mudmk2.game.adapter.in;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jefflife.mudmk2.game.application.domain.model.map.Direction;
import com.jefflife.mudmk2.game.application.service.model.request.CreateRoomRequest;
import com.jefflife.mudmk2.game.application.service.model.request.LinkRoomRequest;
import com.jefflife.mudmk2.game.application.service.model.request.UpdateRoomRequest;
import com.jefflife.mudmk2.game.application.service.model.response.RoomResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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

    @Test
    @Transactional
    void updateRoom_shouldUpdateRoomAndReturnOkResponse() throws Exception {
        // Given
        long areaId = 1L;
        String originalSummary = "Original Room";
        String originalDescription = "This is the original room description";

        // Create a room
        RoomResponse createdRoom = createRoom(areaId, originalSummary, originalDescription);
        long roomId = createdRoom.id();

        // Now update the room
        String updatedSummary = "Updated Room";
        String updatedDescription = "This is the updated room description";

        // When
        RoomResponse updateResponse = updateRoom(roomId, updatedSummary, updatedDescription);

        // Then
        assertThat(updateResponse.id()).isEqualTo(roomId);
        assertThat(updateResponse.areaId()).isEqualTo(areaId);
        assertThat(updateResponse.summary()).isEqualTo(updatedSummary);
        assertThat(updateResponse.description()).isEqualTo(updatedDescription);
    }

    @Test
    @Transactional
    void getRoom_shouldReturnRoomAndOkResponse() throws Exception {
        // Given
        long areaId = 1L;
        String summary = "Test Room for Get";
        String description = "This is a test room for the get method";

        // Create a room
        RoomResponse createdRoom = createRoom(areaId, summary, description);
        long roomId = createdRoom.id();

        // When
        RoomResponse getResponse = getRoom(roomId);

        // Then
        assertThat(getResponse.id()).isEqualTo(roomId);
        assertRoomEquals(getResponse, areaId, summary, description);
    }

    @Test
    @Transactional
    void getRooms_shouldReturnPagedRoomsAndOkResponse() throws Exception {
        // Given
        long areaId = 1L;

        // Create two rooms with unique identifiers
        String summary1 = "Test Room for Pagination 1 " + System.currentTimeMillis();
        String description1 = "This is test room 1 for pagination test";
        RoomResponse room1 = createRoom(areaId, summary1, description1);

        String summary2 = "Test Room for Pagination 2 " + System.currentTimeMillis();
        String description2 = "This is test room 2 for pagination test";
        RoomResponse room2 = createRoom(areaId, summary2, description2);

        // When
        // Request with a large page size to ensure we get all rooms
        Map<String, Object> pageResponse = getPagedRooms(0, 100);

        // Then
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

            if (roomId == room1.id()) {
                assertThat(room.get("summary")).isEqualTo(summary1);
                assertThat(room.get("description")).isEqualTo(description1);
                foundRoom1 = true;
            } else if (roomId == room2.id()) {
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
        assertThat(foundRoom1).withFailMessage("Room 1 (ID: %d) was not found in the response", room1.id()).isTrue();
        assertThat(foundRoom2).withFailMessage("Room 2 (ID: %d) was not found in the response", room2.id()).isTrue();
    }

    @Test
    @Transactional
    void deleteRoom_shouldDeleteRoomAndReturnNoContent() throws Exception {
        // Given
        long areaId = 1L;
        String summary = "Test Room for Delete";
        String description = "This is a test room for the delete method";

        // Create a room
        RoomResponse createdRoom = createRoom(areaId, summary, description);
        long roomId = createdRoom.id();

        // When
        deleteRoom(roomId);

        // Then
        // Verify that the room was actually deleted by checking it's not in the list of all rooms
        Map<String, Object> pageResponse = getPagedRooms(0, 100);
        List<Map<String, Object>> content = (List<Map<String, Object>>) pageResponse.get("content");

        // Check that the deleted room is not in the list
        boolean roomExists = content.stream()
                .anyMatch(room -> ((Number) room.get("id")).longValue() == roomId);

        assertThat(roomExists).withFailMessage("Room (ID: %d) was found in the response but should have been deleted", roomId).isFalse();
    }

    @Test
    @Transactional
    void linkRooms_shouldLinkTwoRoomsAndReturnOkResponse() throws Exception {
        // Given
        long areaId = 1L;

        // Create two rooms
        String summary1 = "East Room";
        String description1 = "This is the east room";
        RoomResponse room1 = createRoom(areaId, summary1, description1);
        long room1Id = room1.id();

        String summary2 = "West Room";
        String description2 = "This is the west room";
        RoomResponse room2 = createRoom(areaId, summary2, description2);
        long room2Id = room2.id();

        // When
        // Link the rooms (Room1 east to Room2, Room2 west to Room1)
        Map<String, Object> linkResponse = linkRooms(room1Id, room2Id, Direction.EAST, Direction.WEST);

        // Then
        // Verify the response contains linked rooms
        List<Map<String, Object>> linkedRooms = (List<Map<String, Object>>) linkResponse.get("linkedRooms");
        assertThat(linkedRooms).hasSize(2);

        // Find Room1 and Room2 in the response
        Map<String, Object> responseRoom1 = null;
        Map<String, Object> responseRoom2 = null;

        for (Map<String, Object> room : linkedRooms) {
            long roomId = ((Number) room.get("id")).longValue();
            if (roomId == room1Id) {
                responseRoom1 = room;
            } else if (roomId == room2Id) {
                responseRoom2 = room;
            }
        }

        assertThat(responseRoom1).isNotNull();
        assertThat(responseRoom2).isNotNull();

        // Verify Room1 has an exit to Room2 in the EAST direction
        List<Map<String, Object>> wayOuts1 = (List<Map<String, Object>>) responseRoom1.get("wayOuts");
        assertThat(wayOuts1).isNotEmpty();

        boolean hasEastExit = wayOuts1.stream()
                .anyMatch(wayOut ->
                        ((Number) wayOut.get("nextRoomId")).longValue() == room2Id &&
                                "EAST".equals(wayOut.get("direction")));

        assertThat(hasEastExit).withFailMessage("Room1 should have an exit to Room2 in the EAST direction").isTrue();

        // Verify Room2 has an exit to Room1 in the WEST direction
        List<Map<String, Object>> wayOuts2 = (List<Map<String, Object>>) responseRoom2.get("wayOuts");
        assertThat(wayOuts2).isNotEmpty();

        boolean hasWestExit = wayOuts2.stream()
                .anyMatch(wayOut ->
                        ((Number) wayOut.get("nextRoomId")).longValue() == room1Id &&
                                "WEST".equals(wayOut.get("direction")));

        assertThat(hasWestExit).withFailMessage("Room2 should have an exit to Room1 in the WEST direction").isTrue();
    }

    private String createRoomJson(long areaId, String summary, String description) throws Exception {
        CreateRoomRequest request = new CreateRoomRequest(areaId, summary, description);
        return objectMapper.writeValueAsString(request);
    }

    private void assertRoomEquals(RoomResponse room, long expectedAreaId, String expectedSummary, String expectedDescription) {
        assertThat(room).isNotNull();
        assertThat(room.areaId()).isEqualTo(expectedAreaId);
        assertThat(room.summary()).isEqualTo(expectedSummary);
        assertThat(room.description()).isEqualTo(expectedDescription);
    }

    private String updateRoomJson(String summary, String description) throws Exception {
        UpdateRoomRequest request = new UpdateRoomRequest(summary, description);
        return objectMapper.writeValueAsString(request);
    }

    private String createLinkRoomJson(Long sourceRoomId, Long destinationRoomId, Direction sourceDir, Direction destinationDir) throws Exception {
        LinkRoomRequest request = new LinkRoomRequest(sourceRoomId, destinationRoomId, sourceDir, destinationDir);
        return objectMapper.writeValueAsString(request);
    }

    private void deleteRoom(Long roomId) throws Exception {
        mockMvc.perform(delete(BASE_URL + "/" + roomId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    /**
     * Helper method to create a room and return the response
     */
    private RoomResponse createRoom(long areaId, String summary, String description) throws Exception {
        String requestJson = createRoomJson(areaId, summary, description);

        MvcResult result = mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andReturn();

        String responseJson = result.getResponse().getContentAsString();
        return objectMapper.readValue(responseJson, RoomResponse.class);
    }

    /**
     * Helper method to get a room by ID
     */
    private RoomResponse getRoom(long roomId) throws Exception {
        MvcResult result = mockMvc.perform(get(BASE_URL + "/" + roomId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String responseJson = result.getResponse().getContentAsString();
        return objectMapper.readValue(responseJson, RoomResponse.class);
    }

    /**
     * Helper method to update a room and return the response
     */
    private RoomResponse updateRoom(long roomId, String summary, String description) throws Exception {
        String requestJson = updateRoomJson(summary, description);

        MvcResult result = mockMvc.perform(patch(BASE_URL + "/" + roomId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andReturn();

        String responseJson = result.getResponse().getContentAsString();
        return objectMapper.readValue(responseJson, RoomResponse.class);
    }

    /**
     * Helper method to link two rooms and return the response
     */
    private Map<String, Object> linkRooms(Long sourceRoomId, Long destinationRoomId, Direction sourceDir, Direction destinationDir) throws Exception {
        String requestJson = createLinkRoomJson(sourceRoomId, destinationRoomId, sourceDir, destinationDir);

        MvcResult result = mockMvc.perform(post(BASE_URL + "/link")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andReturn();

        String responseJson = result.getResponse().getContentAsString();
        return objectMapper.readValue(responseJson, new TypeReference<Map<String, Object>>() {});
    }

    /**
     * Helper method to get paged rooms
     */
    private Map<String, Object> getPagedRooms(int page, int size) throws Exception {
        MvcResult result = mockMvc.perform(get(BASE_URL)
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String responseJson = result.getResponse().getContentAsString();
        return objectMapper.readValue(responseJson, new TypeReference<Map<String, Object>>() {});
    }
}
