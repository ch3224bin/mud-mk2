package com.jefflife.mudmk2.gamedata.adapter.in;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.NPCType;
import com.jefflife.mudmk2.gamedata.application.service.model.request.CreateNonPlayerCharacterRequest;
import com.jefflife.mudmk2.gamedata.application.service.model.response.NonPlayerCharacterResponse;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "test@example.com", roles = "USER")
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
        int hp = 100;
        int maxHp = 100;
        int mp = 50;
        int maxMp = 50;
        int str = 10;
        int dex = 10;
        int con = 10;
        int intelligence = 10;
        int pow = 10;
        int cha = 10;
        Long roomId = 1L;
        int level = 1;
        long experience = 0;
        long nextLevelExp = 100;
        boolean conversable = true;
        String persona = "Common NPC";
        NPCType npcType = NPCType.COMMON;
        Long spawnRoomId = 1L;
        boolean essential = true;

        String requestJson = createNonPlayerCharacterJson(
                name, background, hp, maxHp, mp, maxMp, str, dex, con, intelligence, pow, cha, roomId,
                level, experience, nextLevelExp, conversable, persona, npcType, spawnRoomId, essential
        );

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
        assertThat(response.hp()).isEqualTo(hp);
        assertThat(response.maxHp()).isEqualTo(maxHp);
        assertThat(response.mp()).isEqualTo(mp);
        assertThat(response.maxMp()).isEqualTo(maxMp);
        assertThat(response.str()).isEqualTo(str);
        assertThat(response.dex()).isEqualTo(dex);
        assertThat(response.con()).isEqualTo(con);
        assertThat(response.intelligence()).isEqualTo(intelligence);
        assertThat(response.pow()).isEqualTo(pow);
        assertThat(response.cha()).isEqualTo(cha);
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

    private String createNonPlayerCharacterJson(
            String name, String background, int hp, int maxHp, int mp, int maxMp,
            int str, int dex, int con, int intelligence, int pow, int cha, Long roomId,
            int level, long experience, long nextLevelExp, boolean conversable,
            String persona, NPCType npcType, Long spawnRoomId, boolean essential
    ) throws Exception {
        CreateNonPlayerCharacterRequest request = new CreateNonPlayerCharacterRequest(
                name, background, hp, maxHp, mp, maxMp, str, dex, con, intelligence, pow, cha, roomId,
                level, experience, nextLevelExp, conversable, persona, npcType, spawnRoomId, essential
        );
        return objectMapper.writeValueAsString(request);
    }
}
