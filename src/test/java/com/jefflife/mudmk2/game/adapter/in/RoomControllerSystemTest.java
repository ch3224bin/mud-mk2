package com.jefflife.mudmk2.game.adapter.in;

import com.fasterxml.jackson.databind.ObjectMapper;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
}
