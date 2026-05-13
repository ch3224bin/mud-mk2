package com.jefflife.mudmk2.gamedata.adapter.webapi;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import com.jefflife.mudmk2.config.SecurityMockMvcConfiguration;
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

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@WithMockUser(username = "test@example.com", roles = "USER")
@Import(SecurityMockMvcConfiguration.class)
class ItemInstanceControllerSystemTest {

    private static final String BASE_URL = "/api/v1/item-instances";

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @Test
    void place_withInvalidTemplateId_shouldReturn400() throws Exception {
        final String body = """
                {"templateId": 999999, "quantity": 1, "locationType": "ROOM", "locationId": "1"}
                """;

        final MvcResult result = mockMvc.perform(post(BASE_URL + "/place")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isBadRequest())
            .andReturn();

        final Map<String, Object> response = objectMapper.readValue(
            result.getResponse().getContentAsString(), new TypeReference<>() {});
        assertThat(response.get("success")).isEqualTo(false);
    }

    @Test
    void place_withInvalidLocationId_shouldReturn400() throws Exception {
        final String body = """
                {"templateId": 999999, "quantity": 1, "locationType": "ROOM", "locationId": "not-a-number"}
                """;

        mockMvc.perform(post(BASE_URL + "/place")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isBadRequest());
    }
}
