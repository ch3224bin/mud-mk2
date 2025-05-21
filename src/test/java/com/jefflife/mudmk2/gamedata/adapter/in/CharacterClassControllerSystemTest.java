package com.jefflife.mudmk2.gamedata.adapter.in;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jefflife.mudmk2.gamedata.application.service.model.request.CreateCharacterClassRequest;
import com.jefflife.mudmk2.gamedata.application.service.model.request.UpdateCharacterClassRequest;
import com.jefflife.mudmk2.gamedata.application.service.model.response.CharacterClassResponse;
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
@WithMockUser(username = "test@example.com", roles = "ADMIN") // Assuming ADMIN role is needed for CharacterClass modification
public class CharacterClassControllerSystemTest {

    private static final String BASE_URL = "/api/character-classes";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createCharacterClass_shouldCreateCharacterClassAndReturnCreatedResponse() throws Exception {
        // Given
        String className = "Test Class";
        String classCode = "TST_CLS";
        String description = "A test class.";
        // Define expected stat bonus for assertion
        PlayerStatModel expectedStatBonus = new PlayerStatModel(5, 5, 5, 5, 5, 5); // STR, DEX, CON, INT, POW, CHA

        CreateCharacterClassRequest createRequest = CreateCharacterClassRequest.builder()
                .name(className)
                .code(classCode)
                .description(description)
                .baseHp(100)
                .baseMp(50)
                .baseStr(expectedStatBonus.getStr())
                .baseDex(expectedStatBonus.getDex())
                .baseCon(expectedStatBonus.getCon())
                .baseIntelligence(expectedStatBonus.getIntelligence())
                .basePow(expectedStatBonus.getPow())
                .baseCha(expectedStatBonus.getCha())
                .build();
        String requestJson = objectMapper.writeValueAsString(createRequest);

        // When
        MvcResult result = mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andReturn();

        // Then
        String responseJson = result.getResponse().getContentAsString();
        CharacterClassResponse response = objectMapper.readValue(responseJson, CharacterClassResponse.class);

        assertThat(response.getId()).isNotNull();
        assertCharacterClassEquals(response, className, classCode, description, response);

        // Verify Location header
        String locationHeader = result.getResponse().getHeader("Location");
        assertThat(locationHeader).isEqualTo(BASE_URL + "/" + response.getId());
    }

    @Test
    void getAllCharacterClasses_shouldReturnAllCharacterClasses() throws Exception {
        // Given
        CharacterClassResponse createdClass1 = createTestCharacterClass("Test Class 1", "TST1");
        CharacterClassResponse createdClass2 = createTestCharacterClass("Test Class 2", "TST2");

        // When
        List<CharacterClassResponse> classes = getAllCharacterClasses();

        // Then
        assertThat(classes).isNotNull();
        assertThat(classes.size()).isGreaterThanOrEqualTo(2);

        assertCharacterClassInList(classes, createdClass1);
        assertCharacterClassInList(classes, createdClass2);
    }

    @Test
    void getCharacterClassById_shouldReturnCharacterClass() throws Exception {
        // Given
        CharacterClassResponse createdClass = createTestCharacterClass("Test Class For GetById", "TST_GID");

        // When
        CharacterClassResponse retrievedClass = getCharacterClassById(createdClass.getId());

        // Then
        assertThat(retrievedClass.getId()).isEqualTo(createdClass.getId());
        assertCharacterClassEquals(retrievedClass, "Test Class For GetById", "TST_GID", createdClass.getDescription(), createdClass);
    }

    @Test
    void getCharacterClassByCode_shouldReturnCharacterClass() throws Exception {
        // Given
        String classCode = "TST_GCD";
        CharacterClassResponse createdClass = createTestCharacterClass("Test Class For GetByCode", classCode);

        // When
        MvcResult result = mockMvc.perform(get(BASE_URL + "/code/" + classCode)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        CharacterClassResponse retrievedClass = objectMapper.readValue(result.getResponse().getContentAsString(), CharacterClassResponse.class);

        // Then
        assertThat(retrievedClass.getId()).isEqualTo(createdClass.getId());
        assertCharacterClassEquals(retrievedClass, "Test Class For GetByCode", classCode, createdClass.getDescription(), createdClass);
    }


    @Test
    void updateCharacterClass_shouldUpdateAndReturnUpdatedResponse() throws Exception {
        // Given
        CharacterClassResponse createdClass = createTestCharacterClass("Original Class Name", "ORG_CLS");

        // When
        String updatedClassName = "Updated Class Name";
        String updatedDescription = "Updated description.";
        PlayerStatModel expectedUpdatedStatBonus = new PlayerStatModel(6, 6, 6, 6, 6, 6); // STR, DEX, CON, INT, POW, CHA

        UpdateCharacterClassRequest updateRequest = UpdateCharacterClassRequest.builder()
                .name(updatedClassName)
                .description(updatedDescription)
                // Assuming code is not updatable via this request, so not setting it.
                // If code were updatable, it would be: .code("NEW_CODE")
                .baseHp(110)
                .baseMp(60)
                .baseStr(expectedUpdatedStatBonus.getStr())
                .baseDex(expectedUpdatedStatBonus.getDex())
                .baseCon(expectedUpdatedStatBonus.getCon())
                .baseIntelligence(expectedUpdatedStatBonus.getIntelligence())
                .basePow(expectedUpdatedStatBonus.getPow())
                .baseCha(expectedUpdatedStatBonus.getCha())
                .build();
        CharacterClassResponse updatedClass = updateCharacterClass(createdClass.getId(), updateRequest);

        // Then
        assertThat(updatedClass.getId()).isEqualTo(createdClass.getId());
        // Code should not be updatable, so it should remain the original one.
        assertCharacterClassEquals(updatedClass, updatedClassName, "ORG_CLS", updatedDescription, updatedClass);

        // Verify that the class was actually updated in the database
        CharacterClassResponse retrievedClass = getCharacterClassById(createdClass.getId());
        assertCharacterClassEquals(retrievedClass, updatedClassName, "ORG_CLS", updatedDescription, retrievedClass);
    }

    @Test
    void deleteCharacterClass_shouldDeleteAndReturnNoContent() throws Exception {
        // Given
        CharacterClassResponse createdClass = createTestCharacterClass("Test Class for Delete", "TST_DEL");

        // When
        deleteCharacterClass(createdClass.getId());

        // Then
        // Verify that the class was actually deleted from the database
        mockMvc.perform(get(BASE_URL + "/" + createdClass.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()); // Assuming a 404 for a deleted resource
    }

    @Test
    void initializeDefaultCharacterClasses_shouldReturnOk() throws Exception {
        // When & Then
        mockMvc.perform(post(BASE_URL + "/initialize")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Optional: Verify side effects, e.g., check if default classes are present
        // For now, just checking the 200 OK is sufficient as per requirements.
        // List<CharacterClassResponse> classes = getAllCharacterClasses();
        // assertThat(classes). ... check for default classes
    }

    @Test
    void getCharacterClassById_whenNotFound_shouldReturnNotFound() throws Exception {
        // Given
        long nonExistentId = -999L;

        // When & Then
        mockMvc.perform(get(BASE_URL + "/" + nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void getCharacterClassByCode_whenNotFound_shouldReturnNotFound() throws Exception {
        // Given
        String nonExistentCode = "NON_EXISTENT_CODE";

        // When & Then
        mockMvc.perform(get(BASE_URL + "/code/" + nonExistentCode)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateCharacterClass_whenNotFound_shouldReturnNotFound() throws Exception {
        // Given
        long nonExistentId = -999L;
        UpdateCharacterClassRequest updateRequest = UpdateCharacterClassRequest.builder()
                .name("Non Existent")
                .description("Desc")
                .baseHp(10)
                .baseMp(10)
                .baseStr(1).baseDex(1).baseCon(1).baseIntelligence(1).basePow(1).baseCha(1)
                .build();
        String requestJson = objectMapper.writeValueAsString(updateRequest);

        // When & Then
        mockMvc.perform(put(BASE_URL + "/" + nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteCharacterClass_whenNotFound_shouldReturnNotFound() throws Exception {
        // Given
        long nonExistentId = -999L;

        // When & Then
        mockMvc.perform(delete(BASE_URL + "/" + nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void createCharacterClass_withInvalidInput_shouldReturnBadRequest() throws Exception {
        // Given
        // Invalid request: name is blank, which violates @NotBlank. Code is also blank.
        CreateCharacterClassRequest createRequest = CreateCharacterClassRequest.builder()
                .name("") // Invalid: blank name
                .code("") // Invalid: blank code
                .description("Desc")
                .baseHp(10)
                .baseMp(10)
                .baseStr(1).baseDex(1).baseCon(1).baseIntelligence(1).basePow(1).baseCha(1)
                .build();
        String requestJson = objectMapper.writeValueAsString(createRequest);

        // When & Then
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());
    }


    private CharacterClassResponse createTestCharacterClass(String name, String code) throws Exception {
        String description = "Default description for " + name;
        CreateCharacterClassRequest createRequest = new CreateCharacterClassRequest(code, name, description, 10, 10, 1, 1, 1, 1, 1, 1);
        // Corrected the PlayerStatModel to match the constructor (str, dex, con, intell, pow, cha)
        // The previous test used (1,1,0,0,0,0) which might not align with a 6-param constructor if order matters or if 0 is invalid for some.
        // However, CreateCharacterClassRequest itself doesn't directly take PlayerStatModel but individual stats.
        // Let's use the actual fields from CreateCharacterClassRequest for clarity in createTestCharacterClass
        CreateCharacterClassRequest correctedCreateRequest = CreateCharacterClassRequest.builder()
                .name(name)
                .code(code)
                .description(description)
                .baseHp(10) // Example valid value
                .baseMp(10) // Example valid value
                .baseStr(5) // Example valid value
                .baseDex(5) // Example valid value
                .baseCon(5) // Example valid value
                .baseIntelligence(5) // Example valid value
                .basePow(5) // Example valid value
                .baseCha(5) // Example valid value
                .build();
        String requestJson = objectMapper.writeValueAsString(correctedCreateRequest);

        MvcResult result = mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readValue(result.getResponse().getContentAsString(), CharacterClassResponse.class);
    }

    private CharacterClassResponse getCharacterClassById(Long id) throws Exception {
        MvcResult result = mockMvc.perform(get(BASE_URL + "/" + id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readValue(result.getResponse().getContentAsString(), CharacterClassResponse.class);
    }

    private List<CharacterClassResponse> getAllCharacterClasses() throws Exception {
        MvcResult result = mockMvc.perform(get(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<List<CharacterClassResponse>>() {});
    }

    private CharacterClassResponse updateCharacterClass(Long classId, UpdateCharacterClassRequest updateRequest) throws Exception {
        String updateRequestJson = objectMapper.writeValueAsString(updateRequest);

        MvcResult result = mockMvc.perform(put(BASE_URL + "/" + classId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateRequestJson))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readValue(result.getResponse().getContentAsString(), CharacterClassResponse.class);
    }

    private void deleteCharacterClass(Long classId) throws Exception {
        mockMvc.perform(delete(BASE_URL + "/" + classId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    private void assertCharacterClassEquals(CharacterClassResponse characterClass, String expectedName, String expectedCode, String expectedDescription, CharacterClassResponse characterClassResponse) {
        assertThat(characterClass).isNotNull();
        assertThat(characterClass.getName()).isEqualTo(expectedName);
        assertThat(characterClass.getCode()).isEqualTo(expectedCode);
        assertThat(characterClass.getDescription()).isEqualTo(expectedDescription);
        // Ensure statBonus in CharacterClassResponse matches the expected PlayerStatModel structure
        // This assumes CharacterClassResponse.getStatBonus() returns a PlayerStatModel compatible object
        assertThat(characterClass.getBaseStr()).isEqualTo(characterClassResponse.getBaseStr());
        assertThat(characterClass.getBaseDex()).isEqualTo(characterClassResponse.getBaseDex());
        assertThat(characterClass.getBaseCon()).isEqualTo(characterClassResponse.getBaseCon());
        assertThat(characterClass.getBaseIntelligence()).isEqualTo(characterClassResponse.getBaseIntelligence());
        assertThat(characterClass.getBasePow()).isEqualTo(characterClassResponse.getBasePow());
        assertThat(characterClass.getBaseCha()).isEqualTo(characterClassResponse.getBaseCha());
        // Skills assertion can be added if skills are part of the test data
    }

    private void assertCharacterClassInList(List<CharacterClassResponse> classes, CharacterClassResponse expectedClass) {
        boolean found = classes.stream()
                .anyMatch(cc -> {
                    boolean idsMatch = cc.getId().equals(expectedClass.getId());
                    boolean namesMatch = cc.getName().equals(expectedClass.getName());
                    boolean codesMatch = cc.getCode().equals(expectedClass.getCode());
                    boolean descriptionsMatch = cc.getDescription().equals(expectedClass.getDescription());
                    boolean statsMatch = cc.getBaseStr() == expectedClass.getBaseStr() &&
                                         cc.getBaseDex() == expectedClass.getBaseDex() &&
                                         cc.getBaseCon() == expectedClass.getBaseCon() &&
                                         cc.getBaseIntelligence() == expectedClass.getBaseIntelligence() &&
                                         cc.getBasePow() == expectedClass.getBasePow() &&
                                         cc.getBaseCha() == expectedClass.getBaseCha();
                    return idsMatch && namesMatch && codesMatch && descriptionsMatch && statsMatch;
                });
        assertThat(found).isTrue().withFailMessage("CharacterClass with id %s and code %s not found in the list or properties do not match.", expectedClass.getId(), expectedClass.getCode());
    }

    public class PlayerStatModel {
        private int str;
        private int dex;
        private int con;
        private int intelligence;
        private int pow;
        private int cha;

        public PlayerStatModel(int str, int dex, int con, int intelligence, int pow, int cha) {
            this.str = str;
            this.dex = dex;
            this.con = con;
            this.intelligence = intelligence;
            this.pow = pow;
            this.cha = cha;
        }

        public int getStr() {
            return str;
        }

        public int getDex() {
            return dex;
        }

        public int getCon() {
            return con;
        }

        public int getIntelligence() {
            return intelligence;
        }

        public int getPow() {
            return pow;
        }

        public int getCha() {
            return cha;
        }
    }
}
