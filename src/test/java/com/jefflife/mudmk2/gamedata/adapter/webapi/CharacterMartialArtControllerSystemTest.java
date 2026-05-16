package com.jefflife.mudmk2.gamedata.adapter.webapi;

import com.jefflife.mudmk2.config.SecurityMockMvcConfiguration;
import com.jefflife.mudmk2.gamedata.application.domain.model.item.StatType;
import com.jefflife.mudmk2.gamedata.application.domain.model.item.WeaponType;
import com.jefflife.mudmk2.gamedata.application.domain.model.martialart.MentalMethodKind;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.CharacterClass;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.PlayerCharacter;
import com.jefflife.mudmk2.gamedata.application.service.PlayerCharacterService;
import com.jefflife.mudmk2.gamedata.application.service.model.request.ExternalArtLevelEffectRequest;
import com.jefflife.mudmk2.gamedata.application.service.model.request.ExternalArtTemplateRequest;
import com.jefflife.mudmk2.gamedata.application.service.model.request.MentalMethodLevelEffectRequest;
import com.jefflife.mudmk2.gamedata.application.service.model.request.MentalMethodTemplateRequest;
import com.jefflife.mudmk2.gamedata.application.service.model.request.StatModifierRequest;
import com.jefflife.mudmk2.gamedata.application.service.provided.ExternalArtTemplateCreator;
import com.jefflife.mudmk2.gamedata.application.service.provided.MentalMethodTemplateCreator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@WithMockUser(username = "test@example.com", roles = "USER")
@Import(SecurityMockMvcConfiguration.class)
class CharacterMartialArtControllerSystemTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired PlayerCharacterService playerCharacterService;
    @Autowired MentalMethodTemplateCreator mentalCreator;
    @Autowired ExternalArtTemplateCreator externalCreator;

    private UUID createPc() {
        long userId = System.nanoTime();
        PlayerCharacter pc = playerCharacterService.createCharacter(userId, "테스터" + userId, CharacterClass.WARRIOR);
        return pc.getId();
    }

    private Long createMentalTpl() {
        return mentalCreator.create(new MentalMethodTemplateRequest("심법", "d", MentalMethodKind.INNER_POWER, 1,
                List.of(new MentalMethodLevelEffectRequest(1,
                        List.of(new StatModifierRequest(StatType.INNER_POWER, 3)))))).getId();
    }

    private Long createExternalTpl() {
        return externalCreator.create(new ExternalArtTemplateRequest("외공", "d", WeaponType.SWORD, 1,
                List.of(new ExternalArtLevelEffectRequest(1, 1.1, 5, 5, 0)))).getId();
    }

    @Test
    void learnMental_thenStatus_returnsLearned() throws Exception {
        UUID pc = createPc();
        Long tpl = createMentalTpl();
        String body = "{\"templateId\":" + tpl + "}";

        mockMvc.perform(post("/api/v1/player-characters/" + pc + "/martial-arts/mental-methods/learn")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/player-characters/" + pc + "/martial-arts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.learnedMentalMethods.length()").value(1));
    }

    @Test
    void learnMentalTwice_returns409() throws Exception {
        UUID pc = createPc();
        Long tpl = createMentalTpl();
        String body = "{\"templateId\":" + tpl + "}";

        mockMvc.perform(post("/api/v1/player-characters/" + pc + "/martial-arts/mental-methods/learn")
                .contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isCreated());
        mockMvc.perform(post("/api/v1/player-characters/" + pc + "/martial-arts/mental-methods/learn")
                .contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isConflict());
    }

    @Test
    void equipExternal_appendsSlot() throws Exception {
        UUID pc = createPc();
        Long tpl = createExternalTpl();

        mockMvc.perform(post("/api/v1/player-characters/" + pc + "/martial-arts/external-arts/learn")
                .contentType(MediaType.APPLICATION_JSON).content("{\"templateId\":" + tpl + "}"))
                .andExpect(status().isCreated());

        String json = mockMvc.perform(get("/api/v1/player-characters/" + pc + "/martial-arts"))
                .andReturn().getResponse().getContentAsString();
        JsonNode node = objectMapper.readTree(json);
        String learnedId = node.path("learnedExternalArts").get(0).path("id").asText();

        mockMvc.perform(post("/api/v1/player-characters/" + pc + "/martial-arts/external-arts/equip")
                .contentType(MediaType.APPLICATION_JSON).content("{\"learnedId\":\"" + learnedId + "\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/player-characters/" + pc + "/martial-arts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.equippedExternalSlots.length()").value(1));
    }

    @Test
    void unknownCharacter_returns404OnGet() throws Exception {
        UUID pc = UUID.randomUUID();
        mockMvc.perform(get("/api/v1/player-characters/" + pc + "/martial-arts"))
                .andExpect(status().isNotFound());
    }
}
