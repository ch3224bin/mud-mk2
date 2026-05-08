package com.jefflife.mudmk2.gamedata.adapter.webapi;

import tools.jackson.databind.ObjectMapper;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.Gender;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.NPCType;
import com.jefflife.mudmk2.gamedata.application.service.model.request.CreateNonPlayerCharacterRequest;
import com.jefflife.mudmk2.gamedata.adapter.webapi.response.NonPlayerCharacterResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import com.jefflife.mudmk2.config.SecurityMockMvcConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "test@example.com", roles = "USER")
@Import(SecurityMockMvcConfiguration.class)
public class NonPlayerCharacterControllerSystemTest {

    private static final String BASE_URL = "/api/v1/npcs";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @Transactional
    void createNonPlayerCharacter_shouldCreateNPCAndReturnCreatedResponse() throws Exception {
        // Given
        String name = "Test NPC";
        String background = "This is a test NPC";
        Gender gender = Gender.MALE;
        int hp = 100;
        int mp = 50;
        int ap = 80;
        int vigor = 10;
        int physique = 10;  // maxHp = 10×10 = 100
        int agility = 10;   // maxAp = 10×8 = 80
        int intellect = 10;
        int will = 10;
        int meridian = 10;  // maxMp = 10×5 = 50
        int innerPower = 0;
        int specialTechnique = 0;
        int lightStep = 0;
        int fistsAndPalms = 0;
        int swordMethod = 0;
        int bladeMethod = 0;
        int longWeapon = 0;
        int esotericWeapon = 0;
        int archery = 0;
        Long roomId = 1L;
        int level = 1;
        long experience = 0;
        long nextLevelExp = 100;
        boolean conversable = true;
        String persona = "Common NPC";
        NPCType npcType = NPCType.COMMON;
        Long spawnRoomId = 1L;
        boolean essential = true;

        CreateNonPlayerCharacterRequest request = new CreateNonPlayerCharacterRequest(
                name, background, gender,
                hp, mp, ap,
                vigor, physique, agility, intellect, will, meridian,
                innerPower, specialTechnique, lightStep,
                fistsAndPalms, swordMethod, bladeMethod, longWeapon, esotericWeapon, archery,
                roomId,
                level, experience, nextLevelExp, conversable,
                persona, npcType, spawnRoomId, essential
        );
        String requestJson = objectMapper.writeValueAsString(request);

        // When
        MvcResult result = mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andReturn();

        // Then
        String responseJson = result.getResponse().getContentAsString();
        NonPlayerCharacterResponse response = objectMapper.readValue(responseJson, NonPlayerCharacterResponse.class);

        assertThat(response.id()).isNotNull();
        assertThat(response.name()).isEqualTo(name);
        assertThat(response.background()).isEqualTo(background);
        assertThat(response.gender()).isEqualTo(gender);
        assertThat(response.hp()).isEqualTo(hp);
        assertThat(response.maxHp()).isEqualTo(physique * 10 + specialTechnique * 3); // maxHp formula
        assertThat(response.mp()).isEqualTo(mp);
        assertThat(response.maxMp()).isEqualTo(meridian * 5 + innerPower * 3); // maxMp formula
        assertThat(response.vigor()).isEqualTo(vigor);
        assertThat(response.physique()).isEqualTo(physique);
        assertThat(response.agility()).isEqualTo(agility);
        assertThat(response.intellect()).isEqualTo(intellect);
        assertThat(response.will()).isEqualTo(will);
        assertThat(response.meridian()).isEqualTo(meridian);
        assertThat(response.roomId()).isEqualTo(roomId);
        assertThat(response.level()).isEqualTo(level);
        assertThat(response.experience()).isEqualTo(experience);
        assertThat(response.nextLevelExp()).isEqualTo(nextLevelExp);
        assertThat(response.conversable()).isEqualTo(conversable);
        assertThat(response.persona()).isEqualTo(persona);
        assertThat(response.npcType()).isEqualTo(npcType);
        assertThat(response.spawnRoomId()).isEqualTo(spawnRoomId);
        assertThat(response.essential()).isEqualTo(essential);

        // Verify Location header
        String locationHeader = result.getResponse().getHeader("Location");
        assertThat(locationHeader).isEqualTo(BASE_URL + "/" + response.id());
    }
}
