package com.jefflife.mudmk2.gamedata.adapter.webapi;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import com.jefflife.mudmk2.config.SecurityMockMvcConfiguration;
import com.jefflife.mudmk2.gamedata.application.domain.model.item.*;
import com.jefflife.mudmk2.gamedata.application.service.model.request.ItemTemplateRequest;
import com.jefflife.mudmk2.gamedata.application.service.model.request.StatModifierRequest;
import com.jefflife.mudmk2.gamedata.adapter.webapi.response.ItemTemplateResponse;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@WithMockUser(username = "test@example.com", roles = "USER")
@Import(SecurityMockMvcConfiguration.class)
class ItemTemplateControllerSystemTest {

    private static final String BASE_URL = "/api/v1/item-templates";

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @Test
    void createFood_shouldReturn201WithBody() throws Exception {
        ItemTemplateRequest request = new ItemTemplateRequest(
            ItemType.FOOD, "만두", "찐만두", 1, true,
            10, 0, 5, null, null, null, null, null, null, null
        );

        MvcResult result = mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andReturn();

        ItemTemplateResponse response = objectMapper.readValue(
            result.getResponse().getContentAsString(), ItemTemplateResponse.class);
        assertThat(response.id()).isNotNull();
        assertThat(response.name()).isEqualTo("만두");
        assertThat(response.itemType()).isEqualTo(ItemType.FOOD);
        assertThat(response.hpRecovery()).isEqualTo(10);
        assertThat(result.getResponse().getHeader("Location"))
            .isEqualTo(BASE_URL + "/" + response.id());
    }

    @Test
    void createWeapon_shouldReturn201WithStatModifiers() throws Exception {
        ItemTemplateRequest request = new ItemTemplateRequest(
            ItemType.WEAPON, "철검", "날카로운 검", 5, false,
            null, null, null,
            WeaponType.SWORD, null, null,
            List.of(new StatModifierRequest(StatType.SWORD_METHOD, 5)),
            null, null, null
        );

        MvcResult result = mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andReturn();

        ItemTemplateResponse response = objectMapper.readValue(
            result.getResponse().getContentAsString(), ItemTemplateResponse.class);
        assertThat(response.weaponType()).isEqualTo(WeaponType.SWORD);
        assertThat(response.statModifiers()).hasSize(1);
        assertThat(response.statModifiers().get(0).statType()).isEqualTo(StatType.SWORD_METHOD);
    }

    @Test
    void getById_whenNotFound_shouldReturn404() throws Exception {
        mockMvc.perform(get(BASE_URL + "/-999"))
            .andExpect(status().isNotFound());
    }

    @Test
    void getAll_shouldReturnList() throws Exception {
        createFoodTemplate("국밥");
        createFoodTemplate("비빔밥");

        MvcResult result = mockMvc.perform(get(BASE_URL))
            .andExpect(status().isOk())
            .andReturn();

        List<ItemTemplateResponse> list = objectMapper.readValue(
            result.getResponse().getContentAsString(), new TypeReference<>() {});
        assertThat(list.size()).isGreaterThanOrEqualTo(2);
    }

    @Test
    void getAll_withTypeFilter_shouldReturnOnlyMatchingType() throws Exception {
        createFoodTemplate("국밥");
        createWeaponTemplate("도끼");

        MvcResult result = mockMvc.perform(get(BASE_URL + "?type=FOOD"))
            .andExpect(status().isOk())
            .andReturn();

        List<ItemTemplateResponse> list = objectMapper.readValue(
            result.getResponse().getContentAsString(), new TypeReference<>() {});
        assertThat(list).allMatch(r -> r.itemType() == ItemType.FOOD);
    }

    @Test
    void getAll_withNameFilter_shouldReturnMatchingNames() throws Exception {
        createFoodTemplate("만두");
        createFoodTemplate("만두피");
        createFoodTemplate("국밥");

        MvcResult result = mockMvc.perform(get(BASE_URL + "?name=만두"))
            .andExpect(status().isOk())
            .andReturn();

        List<ItemTemplateResponse> list = objectMapper.readValue(
            result.getResponse().getContentAsString(), new TypeReference<>() {});
        assertThat(list).allMatch(r -> r.name().contains("만두"));
    }

    @Test
    void update_shouldReturn200WithUpdatedData() throws Exception {
        ItemTemplateResponse created = createFoodTemplate("만두");
        ItemTemplateRequest updateRequest = new ItemTemplateRequest(
            ItemType.FOOD, "왕만두", "큰 만두", 2, true,
            20, 5, 10, null, null, null, null, null, null, null
        );

        MvcResult result = mockMvc.perform(put(BASE_URL + "/" + created.id())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
            .andExpect(status().isOk())
            .andReturn();

        ItemTemplateResponse updated = objectMapper.readValue(
            result.getResponse().getContentAsString(), ItemTemplateResponse.class);
        assertThat(updated.name()).isEqualTo("왕만두");
        assertThat(updated.hpRecovery()).isEqualTo(20);
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        ItemTemplateResponse created = createFoodTemplate("지울만두");

        mockMvc.perform(delete(BASE_URL + "/" + created.id()))
            .andExpect(status().isNoContent());

        mockMvc.perform(get(BASE_URL + "/" + created.id()))
            .andExpect(status().isNotFound());
    }

    private ItemTemplateResponse createFoodTemplate(String name) throws Exception {
        ItemTemplateRequest request = new ItemTemplateRequest(
            ItemType.FOOD, name, name + " 설명", 1, true,
            10, 0, 0, null, null, null, null, null, null, null
        );
        MvcResult result = mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andReturn();
        return objectMapper.readValue(result.getResponse().getContentAsString(), ItemTemplateResponse.class);
    }

    private ItemTemplateResponse createWeaponTemplate(String name) throws Exception {
        ItemTemplateRequest request = new ItemTemplateRequest(
            ItemType.WEAPON, name, name + " 설명", 5, false,
            null, null, null, WeaponType.SWORD, null, null,
            List.of(), null, null, null
        );
        MvcResult result = mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andReturn();
        return objectMapper.readValue(result.getResponse().getContentAsString(), ItemTemplateResponse.class);
    }
}
