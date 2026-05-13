package com.jefflife.mudmk2.gameplay.application.service.command.look;

import com.jefflife.mudmk2.gamedata.application.domain.model.item.*;
import com.jefflife.mudmk2.gameplay.application.service.model.template.ItemInfoVariables;
import com.jefflife.mudmk2.gameplay.application.service.required.SendItemInfoMessagePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("ItemDescriber 테스트")
class ItemDescriberTest {

    @Mock
    private SendItemInfoMessagePort sendItemInfoMessagePort;

    private ItemDescriber describer;

    private final Long userId = 1L;

    @BeforeEach
    void setUp() {
        describer = new ItemDescriber(sendItemInfoMessagePort);
    }

    @Test
    @DisplayName("LookableType은 ITEM")
    void getLookableType_isITEM() {
        assertThat(describer.getLookableType()).isEqualTo(LookableType.ITEM);
    }

    @Test
    @DisplayName("FOOD 아이템 — 회복값 출력")
    void describe_food_sendsRecoveryFields() {
        FoodTemplate template = FoodTemplate.builder()
                .name("만두").description("찐만두").weight(1).stackable(true)
                .hpRecovery(10).mpRecovery(0).apRecovery(5).build();
        ItemInstance instance = new ItemInstance(template, 3);
        ItemLookable lookable = new ItemLookable(instance, ItemLookable.ItemLocation.ROOM);

        describer.describe(userId, lookable);

        ArgumentCaptor<ItemInfoVariables> captor = ArgumentCaptor.forClass(ItemInfoVariables.class);
        verify(sendItemInfoMessagePort).sendMessage(captor.capture());
        ItemInfoVariables v = captor.getValue();
        assertThat(v.userId()).isEqualTo(userId);
        assertThat(v.name()).isEqualTo("만두");
        assertThat(v.location()).isEqualTo("바닥");
        assertThat(v.typeLabel()).isEqualTo("음식");
        assertThat(v.weight()).isEqualTo(1);
        assertThat(v.quantity()).isEqualTo(3);
        assertThat(v.stackable()).isTrue();
        assertThat(v.hasRecovery()).isTrue();
        assertThat(v.hpRecovery()).isEqualTo(10);
        assertThat(v.mpRecovery()).isEqualTo(0);
        assertThat(v.apRecovery()).isEqualTo(5);
        assertThat(v.statModifiers()).isEmpty();
        assertThat(v.skillRef()).isNull();
        assertThat(v.missionInfo()).isNull();
    }

    @Test
    @DisplayName("FOOD — 회복값 모두 0이면 hasRecovery=false")
    void describe_foodAllZero_hasRecoveryFalse() {
        FoodTemplate template = FoodTemplate.builder()
                .name("물").description("그냥 물").weight(1).stackable(true)
                .hpRecovery(0).mpRecovery(0).apRecovery(0).build();
        ItemInstance instance = new ItemInstance(template, 1);
        describer.describe(userId, new ItemLookable(instance, ItemLookable.ItemLocation.INVENTORY));

        ArgumentCaptor<ItemInfoVariables> captor = ArgumentCaptor.forClass(ItemInfoVariables.class);
        verify(sendItemInfoMessagePort).sendMessage(captor.capture());
        assertThat(captor.getValue().hasRecovery()).isFalse();
        assertThat(captor.getValue().location()).isEqualTo("소지품");
    }

    @Test
    @DisplayName("WEAPON — 타입 라벨에 무기 서브타입, 스탯 목록")
    void describe_weapon() {
        WeaponTemplate template = WeaponTemplate.builder()
                .name("철검").description("날카로운 검").weight(5).stackable(false)
                .weaponType(WeaponType.SWORD)
                .statModifiers(List.of(
                        new StatModifier(StatType.SWORD_METHOD, 5),
                        new StatModifier(StatType.AGILITY, 2)))
                .build();
        ItemInstance instance = new ItemInstance(template, 1);
        describer.describe(userId, new ItemLookable(instance, ItemLookable.ItemLocation.ROOM));

        ArgumentCaptor<ItemInfoVariables> captor = ArgumentCaptor.forClass(ItemInfoVariables.class);
        verify(sendItemInfoMessagePort).sendMessage(captor.capture());
        ItemInfoVariables v = captor.getValue();
        assertThat(v.typeLabel()).isEqualTo("무기(검)");
        assertThat(v.statModifiers()).hasSize(2);
        assertThat(v.statModifiers().get(0).label()).isEqualTo("검술");
        assertThat(v.statModifiers().get(0).value()).isEqualTo(5);
        assertThat(v.statModifiers().get(1).label()).isEqualTo("민첩");
        assertThat(v.statModifiers().get(1).value()).isEqualTo(2);
        assertThat(v.hasRecovery()).isFalse();
    }

    @Test
    @DisplayName("EQUIPMENT — 타입 라벨에 슬롯 한글")
    void describe_equipment() {
        EquipmentTemplate template = EquipmentTemplate.builder()
                .name("철투구").description("튼튼한 투구").weight(3).stackable(false)
                .equipmentSlot(EquipmentSlot.HELMET)
                .statModifiers(List.of(new StatModifier(StatType.PHYSIQUE, 3)))
                .build();
        ItemInstance instance = new ItemInstance(template, 1);
        describer.describe(userId, new ItemLookable(instance, ItemLookable.ItemLocation.ROOM));

        ArgumentCaptor<ItemInfoVariables> captor = ArgumentCaptor.forClass(ItemInfoVariables.class);
        verify(sendItemInfoMessagePort).sendMessage(captor.capture());
        assertThat(captor.getValue().typeLabel()).isEqualTo("장비(투구)");
    }

    @Test
    @DisplayName("ACCESSORY — 타입 라벨에 악세서리 타입")
    void describe_accessory() {
        AccessoryTemplate template = AccessoryTemplate.builder()
                .name("금목걸이").description("빛나는 목걸이").weight(1).stackable(false)
                .accessoryType(AccessoryType.NECKLACE)
                .statModifiers(List.of())
                .build();
        ItemInstance instance = new ItemInstance(template, 1);
        describer.describe(userId, new ItemLookable(instance, ItemLookable.ItemLocation.ROOM));

        ArgumentCaptor<ItemInfoVariables> captor = ArgumentCaptor.forClass(ItemInfoVariables.class);
        verify(sendItemInfoMessagePort).sendMessage(captor.capture());
        ItemInfoVariables v = captor.getValue();
        assertThat(v.typeLabel()).isEqualTo("악세서리(목걸이)");
        assertThat(v.statModifiers()).isEmpty();
    }

    @Test
    @DisplayName("MARTIAL_ARTS_BOOK — skillRef 설정")
    void describe_martialArtsBook() {
        MartialArtsBookTemplate template = MartialArtsBookTemplate.builder()
                .name("태극권 비급").description("초식이 적힌 책").weight(2).stackable(false)
                .skillRef("TAEGEUK_FIST")
                .build();
        ItemInstance instance = new ItemInstance(template, 1);
        describer.describe(userId, new ItemLookable(instance, ItemLookable.ItemLocation.ROOM));

        ArgumentCaptor<ItemInfoVariables> captor = ArgumentCaptor.forClass(ItemInfoVariables.class);
        verify(sendItemInfoMessagePort).sendMessage(captor.capture());
        ItemInfoVariables v = captor.getValue();
        assertThat(v.typeLabel()).isEqualTo("무공서");
        assertThat(v.skillRef()).isEqualTo("TAEGEUK_FIST");
    }

    @Test
    @DisplayName("MISSION — missionInfo 설정")
    void describe_missionItem() {
        MissionItemTemplate template = MissionItemTemplate.builder()
                .name("문 열쇠").description("녹슨 열쇠").weight(1).stackable(false)
                .missionItemType(MissionItemType.KEY)
                .targetRef("door-42")
                .build();
        ItemInstance instance = new ItemInstance(template, 1);
        describer.describe(userId, new ItemLookable(instance, ItemLookable.ItemLocation.ROOM));

        ArgumentCaptor<ItemInfoVariables> captor = ArgumentCaptor.forClass(ItemInfoVariables.class);
        verify(sendItemInfoMessagePort).sendMessage(captor.capture());
        ItemInfoVariables v = captor.getValue();
        assertThat(v.typeLabel()).isEqualTo("임무 아이템");
        assertThat(v.missionInfo()).isEqualTo("열쇠 / door-42");
    }
}
