package com.jefflife.mudmk2.gamedata.adapter.in;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jefflife.mudmk2.gamedata.application.service.model.request.CreateMonsterTypeRequest;
import com.jefflife.mudmk2.gamedata.application.service.model.request.UpdateMonsterTypeRequest;
import com.jefflife.mudmk2.gamedata.application.service.model.response.MonsterTypeResponse;
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
public class MonsterTypeControllerSystemTest {

    private static final String BASE_URL = "/api/v1/monster-types";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createMonsterType_shouldCreateMonsterTypeAndReturnCreatedResponse() throws Exception {
        // Given
        String monsterTypeName = "Test Monster";
        CreateMonsterTypeRequest createRequest = new CreateMonsterTypeRequest(monsterTypeName, 100, 10, 5);
        String requestJson = objectMapper.writeValueAsString(createRequest);

        // When
        MvcResult result = mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andReturn();

        // Then
        String responseJson = result.getResponse().getContentAsString();
        MonsterTypeResponse response = objectMapper.readValue(responseJson, MonsterTypeResponse.class);

        assertThat(response.id()).isNotNull();
        assertMonsterTypeEquals(response, monsterTypeName, 100, 10, 5);

        // Verify Location header
        String locationHeader = result.getResponse().getHeader("Location");
        assertThat(locationHeader).isEqualTo(BASE_URL + "/" + response.id());
    }

    @Test
    void getMonsterType_whenNotFound_shouldReturnNotFound() throws Exception {
        // Given
        long nonExistentId = -999L;

        // When & Then
        mockMvc.perform(get(BASE_URL + "/" + nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateMonsterType_whenNotFound_shouldReturnNotFound() throws Exception {
        // Given
        long nonExistentId = -999L;
        UpdateMonsterTypeRequest updateRequest = new UpdateMonsterTypeRequest("Non Existent", 1, 1, 1);
        String requestJson = objectMapper.writeValueAsString(updateRequest);

        // When & Then
        mockMvc.perform(put(BASE_URL + "/" + nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isNotFound());
    }

    @Test
    void getMonsterTypes_shouldReturnAllMonsterTypes() throws Exception {
        // Given
        MonsterTypeResponse createdMonsterType1 = createTestMonsterType("Test Monster 1", 100, 10, 5);
        MonsterTypeResponse createdMonsterType2 = createTestMonsterType("Test Monster 2", 120, 12, 6);

        // When
        List<MonsterTypeResponse> monsterTypes = getAllMonsterTypes();

        // Then
        assertThat(monsterTypes).isNotNull();
        assertThat(monsterTypes.size()).isGreaterThanOrEqualTo(2);

        // Verify that the created monster types are in the response
        assertMonsterTypeInList(monsterTypes, createdMonsterType1);
        assertMonsterTypeInList(monsterTypes, createdMonsterType2);
    }

    @Test
    void getMonsterType_shouldReturnMonsterTypeById() throws Exception {
        // Given
        MonsterTypeResponse createdMonsterType = createTestMonsterType("Test Monster for GetById", 150, 15, 7);

        // When
        MonsterTypeResponse retrievedMonsterType = getMonsterTypeById(createdMonsterType.id());

        // Then
        assertThat(retrievedMonsterType.id()).isEqualTo(createdMonsterType.id());
        assertMonsterTypeEquals(retrievedMonsterType, "Test Monster for GetById", 150, 15, 7);
    }

    @Test
    void updateMonsterType_shouldUpdateMonsterTypeAndReturnUpdatedResponse() throws Exception {
        // Given
        MonsterTypeResponse createdMonsterType = createTestMonsterType("Original Monster Name", 200, 20, 10);

        // When
        String updatedMonsterTypeName = "Updated Monster Name";
        UpdateMonsterTypeRequest updateRequest = new UpdateMonsterTypeRequest(updatedMonsterTypeName, 220, 22, 11);
        MonsterTypeResponse updatedMonsterType = updateMonsterType(createdMonsterType.id(), updateRequest);

        // Then
        assertThat(updatedMonsterType.id()).isEqualTo(createdMonsterType.id());
        assertMonsterTypeEquals(updatedMonsterType, updatedMonsterTypeName, 220, 22, 11);

        // Verify that the monster type was actually updated in the database
        MonsterTypeResponse retrievedMonsterType = getMonsterTypeById(createdMonsterType.id());
        assertMonsterTypeEquals(retrievedMonsterType, updatedMonsterTypeName, 220, 22, 11);
    }

    @Test
    void deleteMonsterType_shouldDeleteMonsterTypeAndReturnNoContent() throws Exception {
        // Given
        MonsterTypeResponse createdMonsterType = createTestMonsterType("Test Monster for Delete", 50, 5, 2);

        // When
        deleteMonsterType(createdMonsterType.id());

        // Then
        // Verify that the monster type was actually deleted from the database
        mockMvc.perform(get(BASE_URL + "/" + createdMonsterType.id())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteMonsterType_whenNotFound_shouldReturnNotFound() throws Exception {
        // Given
        long nonExistentId = -999L;

        // When & Then
        mockMvc.perform(delete(BASE_URL + "/" + nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    private MonsterTypeResponse createTestMonsterType(String name, int maxHp, int attack, int defense) throws Exception {
        CreateMonsterTypeRequest createRequest = new CreateMonsterTypeRequest(name, maxHp, attack, defense);
        String requestJson = objectMapper.writeValueAsString(createRequest);

        MvcResult result = mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readValue(result.getResponse().getContentAsString(), MonsterTypeResponse.class);
    }

    private MonsterTypeResponse getMonsterTypeById(Long id) throws Exception {
        MvcResult result = mockMvc.perform(get(BASE_URL + "/" + id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readValue(result.getResponse().getContentAsString(), MonsterTypeResponse.class);
    }

    private List<MonsterTypeResponse> getAllMonsterTypes() throws Exception {
        MvcResult result = mockMvc.perform(get(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<List<MonsterTypeResponse>>() {});
    }

    private MonsterTypeResponse updateMonsterType(Long monsterTypeId, UpdateMonsterTypeRequest updateRequest) throws Exception {
        String updateRequestJson = objectMapper.writeValueAsString(updateRequest);

        MvcResult result = mockMvc.perform(put(BASE_URL + "/" + monsterTypeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateRequestJson))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readValue(result.getResponse().getContentAsString(), MonsterTypeResponse.class);
    }

    private void deleteMonsterType(Long monsterTypeId) throws Exception {
        mockMvc.perform(delete(BASE_URL + "/" + monsterTypeId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    private void assertMonsterTypeEquals(MonsterTypeResponse monsterType, String expectedName, int expectedMaxHp, int expectedAttack, int expectedDefense) {
        assertThat(monsterType).isNotNull();
        assertThat(monsterType.name()).isEqualTo(expectedName);
        assertThat(monsterType.maxHp()).isEqualTo(expectedMaxHp);
        assertThat(monsterType.attack()).isEqualTo(expectedAttack);
        assertThat(monsterType.defense()).isEqualTo(expectedDefense);
    }

    private void assertMonsterTypeInList(List<MonsterTypeResponse> monsterTypes, MonsterTypeResponse expectedMonsterType) {
        boolean found = false;
        for (MonsterTypeResponse monsterType : monsterTypes) {
            if (monsterType.id().equals(expectedMonsterType.id())) {
                found = true;
                assertMonsterTypeEquals(monsterType, expectedMonsterType.name(), expectedMonsterType.maxHp(), expectedMonsterType.attack(), expectedMonsterType.defense());
                break;
            }
        }
        assertThat(found).isTrue().withFailMessage("MonsterType with id %s not found in the list.", expectedMonsterType.id());
    }
}
