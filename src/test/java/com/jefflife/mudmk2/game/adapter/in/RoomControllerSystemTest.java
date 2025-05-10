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

import static org.assertj.core.api.Assertions.assertThat;
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
}
