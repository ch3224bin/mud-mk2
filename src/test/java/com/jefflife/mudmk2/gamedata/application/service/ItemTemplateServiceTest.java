package com.jefflife.mudmk2.gamedata.application.service;

import com.jefflife.mudmk2.gamedata.application.domain.model.item.*;
import com.jefflife.mudmk2.gamedata.application.service.model.request.ItemTemplateRequest;
import com.jefflife.mudmk2.gamedata.application.service.model.request.StatModifierRequest;
import com.jefflife.mudmk2.gamedata.application.service.required.ItemTemplateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemTemplateServiceTest {

    @Mock
    private ItemTemplateRepository itemTemplateRepository;

    private ItemTemplateService service;

    @BeforeEach
    void setUp() {
        service = new ItemTemplateService(itemTemplateRepository);
    }

    @Test
    void create_food_shouldSaveAndReturnFoodTemplate() {
        ItemTemplateRequest request = new ItemTemplateRequest(
            ItemType.FOOD, "만두", "찐만두", 1, true,
            10, 0, 5,
            null, null, null, null, null, null, null
        );
        FoodTemplate saved = FoodTemplate.builder()
            .name("만두").description("찐만두").weight(1).stackable(true)
            .hpRecovery(10).mpRecovery(0).apRecovery(5).build();
        when(itemTemplateRepository.save(any())).thenReturn(saved);

        ItemTemplate result = service.create(request);

        assertThat(result).isInstanceOf(FoodTemplate.class);
        FoodTemplate food = (FoodTemplate) result;
        assertThat(food.getName()).isEqualTo("만두");
        assertThat(food.getHpRecovery()).isEqualTo(10);
        assertThat(food.getApRecovery()).isEqualTo(5);
        verify(itemTemplateRepository).save(any(FoodTemplate.class));
    }

    @Test
    void create_weapon_shouldSaveAndReturnWeaponTemplate() {
        ItemTemplateRequest request = new ItemTemplateRequest(
            ItemType.WEAPON, "철검", "날카로운 검", 5, false,
            null, null, null,
            WeaponType.SWORD, null, null,
            List.of(new StatModifierRequest(StatType.SWORD_METHOD, 5)),
            null, null, null
        );
        WeaponTemplate saved = WeaponTemplate.builder()
            .name("철검").description("날카로운 검").weight(5).stackable(false)
            .weaponType(WeaponType.SWORD)
            .statModifiers(List.of(new StatModifier(StatType.SWORD_METHOD, 5)))
            .build();
        when(itemTemplateRepository.save(any())).thenReturn(saved);

        ItemTemplate result = service.create(request);

        assertThat(result).isInstanceOf(WeaponTemplate.class);
        verify(itemTemplateRepository).save(any(WeaponTemplate.class));
    }

    @Test
    void findById_whenNotFound_shouldThrowNoSuchElementException() {
        when(itemTemplateRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(999L))
            .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void findByNameContaining_shouldDelegateToRepository() {
        when(itemTemplateRepository.findByNameContaining("검")).thenReturn(List.of());

        List<ItemTemplate> result = service.findByNameContaining("검");

        assertThat(result).isEmpty();
        verify(itemTemplateRepository).findByNameContaining("검");
    }

    @Test
    void delete_whenNotFound_shouldThrowNoSuchElementException() {
        when(itemTemplateRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(999L))
            .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void update_food_shouldCallUpdateOnTemplate() {
        FoodTemplate existing = FoodTemplate.builder()
            .name("만두").description("찐만두").weight(1).stackable(true)
            .hpRecovery(10).mpRecovery(0).apRecovery(5).build();
        when(itemTemplateRepository.findById(1L)).thenReturn(Optional.of(existing));
        ItemTemplateRequest updateRequest = new ItemTemplateRequest(
            ItemType.FOOD, "왕만두", "큰 만두", 2, true,
            20, 5, 10, null, null, null, null, null, null, null
        );

        ItemTemplate result = service.update(1L, updateRequest);

        assertThat(result).isSameAs(existing);
        assertThat(result.getName()).isEqualTo("왕만두");
        assertThat(((FoodTemplate) result).getHpRecovery()).isEqualTo(20);
    }

    @Test
    void update_whenNotFound_shouldThrowNoSuchElementException() {
        when(itemTemplateRepository.findById(999L)).thenReturn(Optional.empty());

        ItemTemplateRequest request = new ItemTemplateRequest(
            ItemType.FOOD, "만두", "찐만두", 1, true,
            10, 0, 5, null, null, null, null, null, null, null
        );

        assertThatThrownBy(() -> service.update(999L, request))
            .isInstanceOf(NoSuchElementException.class);
    }
}
