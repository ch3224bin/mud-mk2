package com.jefflife.mudmk2.gamedata.adapter.webapi;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jefflife.mudmk2.gamedata.application.domain.model.map.AreaType;
import com.jefflife.mudmk2.gamedata.application.service.model.response.AreaResponse;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "test@example.com", roles = "USER")
public class AreaControllerSystemTest {

    private static final String BASE_URL = "/api/v1/areas";
    private static final String OPEN_MAP = "OPEN_MAP";
    private static final String INSTANCE_MAP = "INSTANCE_MAP";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createArea_shouldCreateAreaAndReturnCreatedResponse() throws Exception {
        // Given
        String areaName = "Test Area";
        String requestJson = createAreaJson(areaName, OPEN_MAP);

        // When
        MvcResult result = mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andReturn();

        // Then
        String responseJson = result.getResponse().getContentAsString();
        AreaResponse response = objectMapper.readValue(responseJson, AreaResponse.class);

        assertThat(response.getId()).isNotNull();
        assertAreaEquals(response, areaName, OPEN_MAP);

        // Verify Location header
        String locationHeader = result.getResponse().getHeader("Location");
        assertThat(locationHeader).isEqualTo(BASE_URL + "/" + response.getId());
    }

    @Test
    void getAreas_shouldReturnAllAreas() throws Exception {
        // Given
        String areaName1 = "Test Area 1";
        AreaResponse createdArea1 = createTestArea(areaName1, OPEN_MAP);

        String areaName2 = "Test Area 2";
        AreaResponse createdArea2 = createTestArea(areaName2, INSTANCE_MAP);

        // When
        List<AreaResponse> areas = getAllAreas();

        // Then
        assertThat(areas).isNotNull();
        assertThat(areas.size()).isGreaterThanOrEqualTo(2);

        // Verify that the created areas are in the response
        assertAreaInList(areas, createdArea1, areaName1, OPEN_MAP);
        assertAreaInList(areas, createdArea2, areaName2, INSTANCE_MAP);
    }
    @Test
    void getArea_shouldReturnAreaById() throws Exception {
        // Given
        String areaName = "Test Area for GetById";
        AreaResponse createdArea = createTestArea(areaName, OPEN_MAP);

        // When
        AreaResponse retrievedArea = getAreaById(createdArea.getId());

        // Then
        assertThat(retrievedArea.getId()).isEqualTo(createdArea.getId());
        assertAreaEquals(retrievedArea, areaName, OPEN_MAP);
    }

    @Test
    void updateArea_shouldUpdateAreaNameAndReturnUpdatedResponse() throws Exception {
        // Given
        String originalAreaName = "Original Area Name";
        AreaResponse createdArea = createTestArea(originalAreaName, OPEN_MAP);

        // When
        String updatedAreaName = "Updated Area Name";
        AreaResponse updatedArea = updateArea(createdArea.getId(), updatedAreaName);

        // Then
        assertThat(updatedArea.getId()).isEqualTo(createdArea.getId());
        assertThat(updatedArea.getName()).isEqualTo(updatedAreaName);
        assertThat(updatedArea.getType()).isEqualTo(AreaType.valueOf(OPEN_MAP));

        // Verify that the area was actually updated in the database
        AreaResponse retrievedArea = getAreaById(createdArea.getId());
        assertThat(retrievedArea.getName()).isEqualTo(updatedAreaName);
    }

    @Test
    void deleteArea_shouldDeleteAreaAndReturnNoContent() throws Exception {
        // Given
        String areaName = "Test Area for Delete";
        AreaResponse createdArea = createTestArea(areaName, OPEN_MAP);

        // When
        deleteArea(createdArea.getId());

        // Then
        // Verify that the area was actually deleted from the database
        List<AreaResponse> areas = getAllAreas();
        assertAreaNotInList(areas, createdArea.getId());
    }

    private String createAreaJson(String name, String type) {
        return "{"
                + "\"name\": \"" + name + "\","
                + "\"type\": \"" + type + "\""
                + "}";
    }

    private String createUpdateAreaJson(String name) {
        return "{"
                + "\"name\": \"" + name + "\""
                + "}";
    }

    private AreaResponse createTestArea(String name, String type) throws Exception {
        String requestJson = createAreaJson(name, type);

        MvcResult result = mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readValue(
                result.getResponse().getContentAsString(), 
                AreaResponse.class);
    }

    private AreaResponse getAreaById(Long id) throws Exception {
        MvcResult result = mockMvc.perform(get(BASE_URL + "/" + id)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readValue(
                result.getResponse().getContentAsString(), 
                AreaResponse.class);
    }

    private List<AreaResponse> getAllAreas() throws Exception {
        MvcResult result = mockMvc.perform(get(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readValue(
                result.getResponse().getContentAsString(), 
                new TypeReference<List<AreaResponse>>() {});
    }

    private void assertAreaEquals(AreaResponse area, String expectedName, String expectedType) {
        assertThat(area).isNotNull();
        assertThat(area.getName()).isEqualTo(expectedName);
        assertThat(area.getType()).isEqualTo(AreaType.valueOf(expectedType));
    }

    private void assertAreaInList(List<AreaResponse> areas, AreaResponse expectedArea, String expectedName, String expectedType) {
        boolean found = false;

        for (AreaResponse area : areas) {
            if (area.getId().equals(expectedArea.getId())) {
                found = true;
                assertAreaEquals(area, expectedName, expectedType);
                break;
            }
        }

        assertThat(found).isTrue();
    }

    private void assertAreaNotInList(List<AreaResponse> areas, Long areaId) {
        boolean areaExists = areas.stream()
                .anyMatch(area -> area.getId().equals(areaId));

        assertThat(areaExists).isFalse();
    }

    private AreaResponse updateArea(Long areaId, String newName) throws Exception {
        String updateRequestJson = createUpdateAreaJson(newName);

        MvcResult result = mockMvc.perform(patch(BASE_URL + "/" + areaId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateRequestJson))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readValue(
                result.getResponse().getContentAsString(), 
                AreaResponse.class);
    }

    private void deleteArea(Long areaId) throws Exception {
        mockMvc.perform(delete(BASE_URL + "/" + areaId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }
}
