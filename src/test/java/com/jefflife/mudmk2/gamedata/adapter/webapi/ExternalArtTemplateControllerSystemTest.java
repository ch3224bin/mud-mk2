package com.jefflife.mudmk2.gamedata.adapter.webapi;

import com.jefflife.mudmk2.config.SecurityMockMvcConfiguration;
import com.jefflife.mudmk2.gamedata.adapter.webapi.response.ExternalArtTemplateResponse;
import com.jefflife.mudmk2.gamedata.application.domain.model.item.WeaponType;
import com.jefflife.mudmk2.gamedata.application.service.model.request.ExternalArtLevelEffectRequest;
import com.jefflife.mudmk2.gamedata.application.service.model.request.ExternalArtTemplateRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@WithMockUser(username = "test@example.com", roles = "USER")
@Import(SecurityMockMvcConfiguration.class)
class ExternalArtTemplateControllerSystemTest {

    private static final String BASE_URL = "/api/v1/external-art-templates";

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    private ExternalArtTemplateRequest sampleReq(String name) {
        return new ExternalArtTemplateRequest(name, "desc", WeaponType.SWORD, 1,
                List.of(new ExternalArtLevelEffectRequest(1, 1.1, 5, 5, 0)));
    }

    @Test
    void create_returns201() throws Exception {
        MvcResult result = mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleReq("발검술"))))
                .andExpect(status().isCreated()).andReturn();

        ExternalArtTemplateResponse resp = objectMapper.readValue(
                result.getResponse().getContentAsString(), ExternalArtTemplateResponse.class);
        assertThat(resp.id()).isNotNull();
        assertThat(resp.weaponType()).isEqualTo(WeaponType.SWORD);
    }

    @Test
    void getById_whenNotFound_returns404() throws Exception {
        mockMvc.perform(get(BASE_URL + "/-999")).andExpect(status().isNotFound());
    }

    @Test
    void list_withWeaponTypeFilter() throws Exception {
        mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleReq("외공1")))).andExpect(status().isCreated());
        mockMvc.perform(get(BASE_URL + "?weaponType=SWORD")).andExpect(status().isOk());
    }
}
