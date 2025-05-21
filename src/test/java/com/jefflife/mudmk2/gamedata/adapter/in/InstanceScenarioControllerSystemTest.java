package com.jefflife.mudmk2.gamedata.adapter.in;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jefflife.mudmk2.gamedata.application.service.model.response.InstanceScenarioResponse; // This might need adjustment based on actual response objects
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
public class InstanceScenarioControllerSystemTest {

    private static final String BASE_URL = "/api/v1/instance-scenarios";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createInstanceScenario_shouldCreateInstanceScenarioAndReturnCreatedResponse() throws Exception {
        // Given
        String title = "Test Scenario";
        String description = "A scenario for testing purposes.";
        long areaId = 1L;
        long entranceRoomId = 101L;
        String requestJson = createInstanceScenarioJson(title, description, areaId, entranceRoomId);

        // When
        MvcResult result = mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andReturn();

        // Then
        String responseJson = result.getResponse().getContentAsString();
        InstanceScenarioResponse response = objectMapper.readValue(responseJson, InstanceScenarioResponse.class);

        assertThat(response.getId()).isNotNull(); // or isGreaterThan(0L)
        assertThat(response.getTitle()).isEqualTo(title);
        assertThat(response.getDescription()).isEqualTo(description);
        assertThat(response.getAreaId()).isEqualTo(areaId);
        assertThat(response.getEntranceRoomId()).isEqualTo(entranceRoomId);

        String locationHeader = result.getResponse().getHeader("Location");
        assertThat(locationHeader).isEqualTo(BASE_URL + "/" + response.getId());
    }

    private String createInstanceScenarioJson(String title, String description, long areaId, long entranceRoomId) {
        return String.format("{\"title\":\"%s\",\"description\":\"%s\",\"areaId\":%d,\"entranceRoomId\":%d}", title, description, areaId, entranceRoomId);
    }

    private InstanceScenarioResponse createTestInstanceScenario(String title, String description, long areaId, long entranceRoomId) throws Exception {
        String requestJson = createInstanceScenarioJson(title, description, areaId, entranceRoomId);

        MvcResult result = mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readValue(result.getResponse().getContentAsString(), InstanceScenarioResponse.class);
    }

    private void assertInstanceScenarioEquals(InstanceScenarioResponse actual, InstanceScenarioResponse expected) {
        assertThat(actual.getTitle()).isEqualTo(expected.getTitle());
        assertThat(actual.getDescription()).isEqualTo(expected.getDescription());
        assertThat(actual.getAreaId()).isEqualTo(expected.getAreaId());
        assertThat(actual.getEntranceRoomId()).isEqualTo(expected.getEntranceRoomId());
    }

    private void assertInstanceScenarioInList(List<InstanceScenarioResponse> scenarios, InstanceScenarioResponse expectedScenario) {
        boolean found = false;
        for (InstanceScenarioResponse scenario : scenarios) {
            if (scenario.getId().equals(expectedScenario.getId())) { // Use .equals() for Long comparison
                assertInstanceScenarioEquals(scenario, expectedScenario);
                found = true;
                break;
            }
        }
        assertThat(found).isTrueWithMessage("Scenario with ID " + expectedScenario.getId() + " not found in the list.");
    }

    @Test
    void getAllInstanceScenarios_shouldReturnAllInstanceScenarios() throws Exception {
        // Given
        InstanceScenarioResponse scenario1 = createTestInstanceScenario("Scenario A", "Desc A", 1L, 101L);
        InstanceScenarioResponse scenario2 = createTestInstanceScenario("Scenario B", "Desc B", 2L, 102L);

        // When
        MvcResult result = mockMvc.perform(get(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        String responseJson = result.getResponse().getContentAsString();
        List<InstanceScenarioResponse> actualScenarios = objectMapper.readValue(responseJson, new TypeReference<List<InstanceScenarioResponse>>() {});

        assertThat(actualScenarios).isNotNull();
        assertThat(actualScenarios.size()).isGreaterThanOrEqualTo(2);

        assertInstanceScenarioInList(actualScenarios, scenario1);
        assertInstanceScenarioInList(actualScenarios, scenario2);
    }

    @Test
    void getInstanceScenarioById_shouldReturnInstanceScenario() throws Exception {
        // Given
        String title = "Scenario For GetById";
        String description = "Details for GetById test";
        long areaId = 3L;
        long entranceRoomId = 103L;
        InstanceScenarioResponse createdScenario = createTestInstanceScenario(title, description, areaId, entranceRoomId);

        // When
        MvcResult result = mockMvc.perform(get(BASE_URL + "/" + createdScenario.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        String responseJson = result.getResponse().getContentAsString();
        InstanceScenarioResponse retrievedScenario = objectMapper.readValue(responseJson, InstanceScenarioResponse.class);

        assertThat(retrievedScenario.getId()).isEqualTo(createdScenario.getId());
        assertInstanceScenarioEquals(retrievedScenario, createdScenario);
    }

    @Test
    void getInstanceScenarioByTitle_shouldReturnInstanceScenario() throws Exception {
        // Given
        String title = "Unique Scenario Title For Lookup";
        String description = "Details for GetByTitle test";
        long areaId = 4L;
        long entranceRoomId = 104L;
        InstanceScenarioResponse createdScenario = createTestInstanceScenario(title, description, areaId, entranceRoomId);

        // When
        MvcResult result = mockMvc.perform(get(BASE_URL + "/title/" + createdScenario.getTitle())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        String responseJson = result.getResponse().getContentAsString();
        InstanceScenarioResponse retrievedScenario = objectMapper.readValue(responseJson, InstanceScenarioResponse.class);

        assertThat(retrievedScenario.getId()).isEqualTo(createdScenario.getId());
        assertInstanceScenarioEquals(retrievedScenario, createdScenario);
    }

    private String updateInstanceScenarioJson(String title, String description, long areaId, long entranceRoomId) {
        return String.format("{\"title\":\"%s\",\"description\":\"%s\",\"areaId\":%d,\"entranceRoomId\":%d}", title, description, areaId, entranceRoomId);
    }

    @Test
    void updateInstanceScenario_shouldUpdateAndReturnUpdatedInstanceScenario() throws Exception {
        // Given
        InstanceScenarioResponse originalScenario = createTestInstanceScenario("Original Title", "Original Desc", 5L, 105L);

        String updatedTitle = "Updated Scenario Title";
        String updatedDescription = "Updated description for the scenario.";
        long updatedAreaId = 6L;
        long updatedEntranceRoomId = 106L;

        String updateRequestJson = updateInstanceScenarioJson(updatedTitle, updatedDescription, updatedAreaId, updatedEntranceRoomId);

        // When
        MvcResult result = mockMvc.perform(patch(BASE_URL + "/" + originalScenario.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateRequestJson))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        String responseJson = result.getResponse().getContentAsString();
        InstanceScenarioResponse updatedScenarioResponse = objectMapper.readValue(responseJson, InstanceScenarioResponse.class);

        InstanceScenarioResponse expectedUpdatedScenario = new InstanceScenarioResponse(originalScenario.getId(), updatedTitle, updatedDescription, updatedAreaId, updatedEntranceRoomId);

        assertThat(updatedScenarioResponse.getId()).isEqualTo(originalScenario.getId());
        assertInstanceScenarioEquals(updatedScenarioResponse, expectedUpdatedScenario);

        // Verify persistence
        MvcResult refetchResult = mockMvc.perform(get(BASE_URL + "/" + originalScenario.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        InstanceScenarioResponse refetchedScenario = objectMapper.readValue(refetchResult.getResponse().getContentAsString(), InstanceScenarioResponse.class);
        assertInstanceScenarioEquals(refetchedScenario, expectedUpdatedScenario);
    }
}
