package com.jefflife.mudmk2.gamedata.adapter.webapi;

import com.jefflife.mudmk2.config.SecurityMockMvcConfiguration;
import com.jefflife.mudmk2.gamedata.adapter.webapi.response.MentalMethodTemplateResponse;
import com.jefflife.mudmk2.gamedata.application.domain.model.item.StatType;
import com.jefflife.mudmk2.gamedata.application.domain.model.martialart.MentalMethodKind;
import com.jefflife.mudmk2.gamedata.application.service.model.request.MentalMethodLevelEffectRequest;
import com.jefflife.mudmk2.gamedata.application.service.model.request.MentalMethodTemplateRequest;
import com.jefflife.mudmk2.gamedata.application.service.model.request.StatModifierRequest;
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
class MentalMethodTemplateControllerSystemTest {

    private static final String BASE_URL = "/api/v1/mental-method-templates";

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    private MentalMethodTemplateRequest sampleReq(String name) {
        return new MentalMethodTemplateRequest(name, "desc", MentalMethodKind.INNER_POWER, 1,
                List.of(new MentalMethodLevelEffectRequest(1,
                        List.of(new StatModifierRequest(StatType.INNER_POWER, 3)))));
    }

    @Test
    void create_returns201WithBody() throws Exception {
        MvcResult result = mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleReq("천뢰신공"))))
                .andExpect(status().isCreated())
                .andReturn();

        MentalMethodTemplateResponse resp = objectMapper.readValue(
                result.getResponse().getContentAsString(), MentalMethodTemplateResponse.class);
        assertThat(resp.id()).isNotNull();
        assertThat(resp.name()).isEqualTo("천뢰신공");
        assertThat(resp.kind()).isEqualTo(MentalMethodKind.INNER_POWER);
        assertThat(result.getResponse().getHeader("Location")).isEqualTo(BASE_URL + "/" + resp.id());
    }

    @Test
    void getById_whenNotFound_returns404() throws Exception {
        mockMvc.perform(get(BASE_URL + "/-999")).andExpect(status().isNotFound());
    }

    @Test
    void delete_whenNotFound_returns404() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/-999")).andExpect(status().isNotFound());
    }

    @Test
    void update_andDelete_flow() throws Exception {
        MvcResult created = mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleReq("심법1"))))
                .andExpect(status().isCreated()).andReturn();
        Long id = objectMapper.readValue(created.getResponse().getContentAsString(),
                MentalMethodTemplateResponse.class).id();

        mockMvc.perform(put(BASE_URL + "/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleReq("심법2"))))
                .andExpect(status().isOk());

        mockMvc.perform(delete(BASE_URL + "/" + id)).andExpect(status().isNoContent());
    }

    @Test
    void list_withKindFilter() throws Exception {
        mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleReq("심법A")))).andExpect(status().isCreated());
        mockMvc.perform(get(BASE_URL + "?kind=INNER_POWER")).andExpect(status().isOk());
    }
}
