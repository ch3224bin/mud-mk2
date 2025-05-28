package com.jefflife.mudmk2.gamedata.adapter.in;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.Gender;
import com.jefflife.mudmk2.gamedata.application.service.model.request.CreateMonsterTypeRequest;
import com.jefflife.mudmk2.gamedata.application.service.model.request.MonsterSpawnRoomRequest;
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
        String description = "Test Monster Description";
        CreateMonsterTypeRequest createRequest = new CreateMonsterTypeRequest(
                monsterTypeName,
                description,
                Gender.MALE, // gender
                100, // baseHp
                50,  // baseMp
                10,  // baseStr
                8,   // baseDex
                5,   // baseCon
                7,   // baseIntelligence
                6,   // basePow
                5,   // baseCha
                100, // baseExperience
                10,  // hpPerLevel
                2,   // strPerLevel
                2,   // dexPerLevel
                1,   // conPerLevel
                1,   // intelligencePerLevel
                1,   // powPerLevel
                1,   // chaPerLevel
                50,  // expPerLevel
                List.of(), // spawnRooms
                1,   // aggressiveness
                60   // respawnTime
        );
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
        UpdateMonsterTypeRequest updateRequest = new UpdateMonsterTypeRequest(
                "Non Existent",     // name
                "Description",      // description
                100,                // baseHp
                50,                 // baseMp
                10,                 // baseStr
                8,                  // baseDex
                5,                  // baseCon
                7,                  // baseIntelligence
                6,                  // basePow
                5,                  // baseCha
                100,                // baseExperience
                10,                 // hpPerLevel
                2,                  // strPerLevel
                2,                  // dexPerLevel
                1,                  // conPerLevel
                1,                  // intelligencePerLevel
                1,                  // powPerLevel
                1,                  // chaPerLevel
                50,                 // expPerLevel
                List.of(),          // spawnRooms
                1,                  // aggressiveness
                60                  // respawnTime
        );
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
        UpdateMonsterTypeRequest updateRequest = new UpdateMonsterTypeRequest(
                updatedMonsterTypeName, // name
                "Updated Description",  // description
                220,                    // baseHp
                60,                     // baseMp
                22,                     // baseStr
                10,                     // baseDex
                11,                     // baseCon
                9,                      // baseIntelligence
                8,                      // basePow
                7,                      // baseCha
                120,                    // baseExperience
                12,                     // hpPerLevel
                3,                      // strPerLevel
                3,                      // dexPerLevel
                2,                      // conPerLevel
                2,                      // intelligencePerLevel
                2,                      // powPerLevel
                2,                      // chaPerLevel
                60,                     // expPerLevel
                List.of(),              // spawnRooms
                2,                      // aggressiveness
                70                      // respawnTime
        );
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

    private MonsterTypeResponse createTestMonsterType(String name, String description, int baseHp, int baseMp, int baseStr, int baseDex, int baseCon, int baseIntelligence, int basePow, int baseCha, long baseExperience, int hpPerLevel, int strPerLevel, int dexPerLevel, int conPerLevel, int intelligencePerLevel, int powPerLevel, int chaPerLevel, int expPerLevel, List<MonsterSpawnRoomRequest> spawnRooms, int aggressiveness, int respawnTime) throws Exception {
        CreateMonsterTypeRequest createRequest = new CreateMonsterTypeRequest(name, description, Gender.MALE, baseHp, baseMp, baseStr, baseDex, baseCon, baseIntelligence, basePow, baseCha, baseExperience, hpPerLevel, strPerLevel, dexPerLevel, conPerLevel, intelligencePerLevel, powPerLevel, chaPerLevel, expPerLevel, spawnRooms, aggressiveness, respawnTime);
        String requestJson = objectMapper.writeValueAsString(createRequest);

        MvcResult result = mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readValue(result.getResponse().getContentAsString(), MonsterTypeResponse.class);
    }

    private MonsterTypeResponse createTestMonsterType(String name, int baseHp, int baseStr, int baseCon) throws Exception {
        CreateMonsterTypeRequest createRequest = new CreateMonsterTypeRequest(
                name,                // name
                "Description for " + name, // description
                Gender.MALE,        // gender
                baseHp,             // baseHp
                50,                 // baseMp
                baseStr,            // baseStr
                8,                  // baseDex
                baseCon,            // baseCon
                7,                  // baseIntelligence
                6,                  // basePow
                5,                  // baseCha
                100,                // baseExperience
                10,                 // hpPerLevel
                2,                  // strPerLevel
                2,                  // dexPerLevel
                1,                  // conPerLevel
                1,                  // intelligencePerLevel
                1,                  // powPerLevel
                1,                  // chaPerLevel
                50,                 // expPerLevel
                List.of(),          // spawnRooms
                1,                  // aggressiveness
                60                  // respawnTime
        );
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

    private void assertMonsterTypeEquals(MonsterTypeResponse monsterType, String expectedName, int expectedBaseHp, int expectedBaseStr, int expectedBaseCon) {
        assertThat(monsterType).isNotNull();
        assertThat(monsterType.name()).isEqualTo(expectedName);
        assertThat(monsterType.baseHp()).isEqualTo(expectedBaseHp);
        assertThat(monsterType.baseStr()).isEqualTo(expectedBaseStr);
        assertThat(monsterType.baseCon()).isEqualTo(expectedBaseCon);
    }

    private void assertMonsterTypeInList(List<MonsterTypeResponse> monsterTypes, MonsterTypeResponse expectedMonsterType) {
        boolean found = false;
        for (MonsterTypeResponse monsterType : monsterTypes) {
            if (monsterType.id().equals(expectedMonsterType.id())) {
                found = true;
                assertMonsterTypeEquals(monsterType, expectedMonsterType.name(), expectedMonsterType.baseHp(), expectedMonsterType.baseStr(), expectedMonsterType.baseCon());
                break;
            }
        }
        assertThat(found).isTrue();
    }
}
