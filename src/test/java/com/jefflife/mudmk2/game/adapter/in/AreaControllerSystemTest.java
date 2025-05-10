package com.jefflife.mudmk2.game.adapter.in;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jefflife.mudmk2.game.application.domain.model.map.AreaType;
import com.jefflife.mudmk2.game.application.service.model.response.AreaResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class AreaControllerSystemTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "test@example.com", roles = "USER")
    void createArea_shouldCreateAreaAndReturnCreatedResponse() throws Exception {
        // Given
        String areaName = "Test Area";
        String areaType = "OPEN_MAP";

        // Create request JSON
        String requestJson = "{"
                + "\"name\": \"" + areaName + "\","
                + "\"type\": \"" + areaType + "\""
                + "}";

        // When
        MvcResult result = mockMvc.perform(post("/api/v1/areas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isCreated())
                .andReturn();

        // Then
        String responseJson = result.getResponse().getContentAsString();
        AreaResponse response = objectMapper.readValue(responseJson, AreaResponse.class);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isNotNull();
        assertThat(response.getName()).isEqualTo(areaName);
        assertThat(response.getType()).isEqualTo(AreaType.valueOf(areaType));

        // Verify Location header
        String locationHeader = result.getResponse().getHeader("Location");
        assertThat(locationHeader).isEqualTo("/api/v1/areas/" + response.getId());
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = "USER")
    void getAreas_shouldReturnAllAreas() throws Exception {
        // Given
        // Create first test area
        String areaName1 = "Test Area 1";
        String areaType1 = "OPEN_MAP";
        String requestJson1 = "{"
                + "\"name\": \"" + areaName1 + "\","
                + "\"type\": \"" + areaType1 + "\""
                + "}";

        MvcResult createResult1 = mockMvc.perform(post("/api/v1/areas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson1))
                .andExpect(status().isCreated())
                .andReturn();

        AreaResponse createdArea1 = objectMapper.readValue(
                createResult1.getResponse().getContentAsString(), 
                AreaResponse.class);

        // Create second test area
        String areaName2 = "Test Area 2";
        String areaType2 = "INSTANCE_MAP";
        String requestJson2 = "{"
                + "\"name\": \"" + areaName2 + "\","
                + "\"type\": \"" + areaType2 + "\""
                + "}";

        MvcResult createResult2 = mockMvc.perform(post("/api/v1/areas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson2))
                .andExpect(status().isCreated())
                .andReturn();

        AreaResponse createdArea2 = objectMapper.readValue(
                createResult2.getResponse().getContentAsString(), 
                AreaResponse.class);

        // When
        MvcResult getResult = mockMvc.perform(get("/api/v1/areas")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        String responseJson = getResult.getResponse().getContentAsString();
        List<AreaResponse> areas = objectMapper.readValue(responseJson, new TypeReference<List<AreaResponse>>() {});

        assertThat(areas).isNotNull();
        assertThat(areas.size()).isGreaterThanOrEqualTo(2);

        // Verify that the created areas are in the response
        boolean foundArea1 = false;
        boolean foundArea2 = false;

        for (AreaResponse area : areas) {
            if (area.getId().equals(createdArea1.getId())) {
                foundArea1 = true;
                assertThat(area.getName()).isEqualTo(areaName1);
                assertThat(area.getType()).isEqualTo(AreaType.valueOf(areaType1));
            } else if (area.getId().equals(createdArea2.getId())) {
                foundArea2 = true;
                assertThat(area.getName()).isEqualTo(areaName2);
                assertThat(area.getType()).isEqualTo(AreaType.valueOf(areaType2));
            }
        }

        assertThat(foundArea1).isTrue();
        assertThat(foundArea2).isTrue();
    }
    @Test
    @WithMockUser(username = "test@example.com", roles = "USER")
    void getArea_shouldReturnAreaById() throws Exception {
        // Given
        // Create a test area
        String areaName = "Test Area for GetById";
        String areaType = "OPEN_MAP";
        String requestJson = "{"
                + "\"name\": \"" + areaName + "\","
                + "\"type\": \"" + areaType + "\""
                + "}";

        MvcResult createResult = mockMvc.perform(post("/api/v1/areas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isCreated())
                .andReturn();

        AreaResponse createdArea = objectMapper.readValue(
                createResult.getResponse().getContentAsString(), 
                AreaResponse.class);

        // When
        MvcResult getResult = mockMvc.perform(get("/api/v1/areas/" + createdArea.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        String responseJson = getResult.getResponse().getContentAsString();
        AreaResponse retrievedArea = objectMapper.readValue(responseJson, AreaResponse.class);

        assertThat(retrievedArea).isNotNull();
        assertThat(retrievedArea.getId()).isEqualTo(createdArea.getId());
        assertThat(retrievedArea.getName()).isEqualTo(areaName);
        assertThat(retrievedArea.getType()).isEqualTo(AreaType.valueOf(areaType));
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = "USER")
    void updateArea_shouldUpdateAreaNameAndReturnUpdatedResponse() throws Exception {
        // Given
        // Create a test area
        String originalAreaName = "Original Area Name";
        String areaType = "OPEN_MAP";
        String createRequestJson = "{"
                + "\"name\": \"" + originalAreaName + "\","
                + "\"type\": \"" + areaType + "\""
                + "}";

        MvcResult createResult = mockMvc.perform(post("/api/v1/areas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createRequestJson))
                .andExpect(status().isCreated())
                .andReturn();

        AreaResponse createdArea = objectMapper.readValue(
                createResult.getResponse().getContentAsString(), 
                AreaResponse.class);

        // Prepare update request with new name
        String updatedAreaName = "Updated Area Name";
        String updateRequestJson = "{"
                + "\"name\": \"" + updatedAreaName + "\""
                + "}";

        // When
        MvcResult updateResult = mockMvc.perform(patch("/api/v1/areas/" + createdArea.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateRequestJson))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        String responseJson = updateResult.getResponse().getContentAsString();
        AreaResponse updatedArea = objectMapper.readValue(responseJson, AreaResponse.class);

        assertThat(updatedArea).isNotNull();
        assertThat(updatedArea.getId()).isEqualTo(createdArea.getId());
        assertThat(updatedArea.getName()).isEqualTo(updatedAreaName);
        assertThat(updatedArea.getType()).isEqualTo(AreaType.valueOf(areaType));

        // Verify that the area was actually updated in the database
        MvcResult getResult = mockMvc.perform(get("/api/v1/areas/" + createdArea.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String getResponseJson = getResult.getResponse().getContentAsString();
        AreaResponse retrievedArea = objectMapper.readValue(getResponseJson, AreaResponse.class);

        assertThat(retrievedArea.getName()).isEqualTo(updatedAreaName);
    }
}
