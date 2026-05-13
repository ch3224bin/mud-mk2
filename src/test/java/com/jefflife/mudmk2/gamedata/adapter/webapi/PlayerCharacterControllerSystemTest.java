package com.jefflife.mudmk2.gamedata.adapter.webapi;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import com.jefflife.mudmk2.config.SecurityMockMvcConfiguration;
import com.jefflife.mudmk2.gamedata.adapter.webapi.response.PlayerCharacterSearchResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@WithMockUser(username = "test@example.com", roles = "USER")
@Import(SecurityMockMvcConfiguration.class)
class PlayerCharacterControllerSystemTest {

    private static final String BASE_URL = PlayerCharacterController.BASE_PATH;

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @Test
    void searchPlayerCharacters_withEmptyNickname_shouldReturn200WithList() throws Exception {
        final MvcResult result = mockMvc.perform(get(BASE_URL + "/search?nickname="))
            .andExpect(status().isOk())
            .andReturn();

        final List<PlayerCharacterSearchResponse> list = objectMapper.readValue(
            result.getResponse().getContentAsString(), new TypeReference<>() {});
        assertThat(list).isNotNull();
    }

    @Test
    void searchPlayerCharacters_withNickname_shouldReturn200() throws Exception {
        final MvcResult result = mockMvc.perform(get(BASE_URL + "/search?nickname=홍길동"))
            .andExpect(status().isOk())
            .andReturn();

        final List<PlayerCharacterSearchResponse> list = objectMapper.readValue(
            result.getResponse().getContentAsString(), new TypeReference<>() {});
        assertThat(list).isNotNull();
    }

    @Test
    void searchPlayerCharacters_withoutParam_shouldReturn200() throws Exception {
        final MvcResult result = mockMvc.perform(get(BASE_URL + "/search"))
            .andExpect(status().isOk())
            .andReturn();

        final List<PlayerCharacterSearchResponse> list = objectMapper.readValue(
            result.getResponse().getContentAsString(), new TypeReference<>() {});
        assertThat(list).isNotNull();
    }
}
