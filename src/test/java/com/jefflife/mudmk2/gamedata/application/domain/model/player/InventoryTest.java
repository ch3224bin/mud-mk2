package com.jefflife.mudmk2.gamedata.application.domain.model.player;

import com.jefflife.mudmk2.gamedata.application.domain.model.item.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

class InventoryTest {

    private Inventory inventory;
    private FoodTemplate foodTemplate;
    private WeaponTemplate swordTemplate;

    @BeforeEach
    void setUp() {
        inventory = Inventory.create(100);
        foodTemplate = FoodTemplate.builder()
                .name("만두").description("맛있는 만두").weight(1).stackable(true)
                .hpRecovery(0).mpRecovery(0).apRecovery(50).build();
        swordTemplate = WeaponTemplate.builder()
                .name("철검").description("평범한 철검").weight(5).stackable(false)
                .weaponType(WeaponType.SWORD).statModifiers(List.of()).build();
    }

    @Test
    void canAdd_returnsTrueWhenWeightFits() {
        assertThat(inventory.canAdd(swordTemplate, 1)).isTrue();
    }

    @Test
    void canAdd_returnsFalseWhenWeightExceeds() {
        for (int i = 0; i < 20; i++) {
            inventory.addItem(new ItemInstance(swordTemplate, 1));
        }
        assertThat(inventory.canAdd(swordTemplate, 1)).isFalse();
    }

    @Test
    void addItem_stackableItem_mergesQuantity() {
        inventory.addItem(new ItemInstance(foodTemplate, 3));
        inventory.addItem(new ItemInstance(foodTemplate, 2));
        assertThat(inventory.getItems()).hasSize(1);
        assertThat(inventory.getItems().get(0).getQuantity()).isEqualTo(5);
    }

    @Test
    void addItem_nonStackableItem_addsSeparateInstances() {
        inventory.addItem(new ItemInstance(swordTemplate, 1));
        inventory.addItem(new ItemInstance(swordTemplate, 1));
        assertThat(inventory.getItems()).hasSize(2);
    }

    @Test
    void removeItem_removesFromList() {
        ItemInstance instance = new ItemInstance(swordTemplate, 1);
        inventory.addItem(instance);
        inventory.removeItem(instance);
        assertThat(inventory.getItems()).isEmpty();
    }

    @Test
    void findItemsByName_returnsMatchingItems() {
        inventory.addItem(new ItemInstance(swordTemplate, 1));
        inventory.addItem(new ItemInstance(swordTemplate, 1));
        List<ItemInstance> found = inventory.findItemsByName("철검");
        assertThat(found).hasSize(2);
    }

    @Test
    void currentWeight_calculatesCorrectly() {
        inventory.addItem(new ItemInstance(swordTemplate, 1)); // 5
        inventory.addItem(new ItemInstance(foodTemplate, 3));  // 1 * 3 = 3
        assertThat(inventory.currentWeight()).isEqualTo(8);
    }
}
