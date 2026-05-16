package com.jefflife.mudmk2.gamedata.application.domain.model.item;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class ItemInstanceTest {

    private FoodTemplate makeFood() {
        return FoodTemplate.builder()
                .name("만두").description("맛있는 만두").weight(1).stackable(true)
                .hpRecovery(0).mpRecovery(0).apRecovery(50)
                .build();
    }

    @Test
    void itemInstance_createdWithQuantityOne_byDefault() {
        FoodTemplate food = makeFood();
        ItemInstance instance = new ItemInstance(food, 1);
        assertThat(instance.getQuantity()).isEqualTo(1);
        assertThat(instance.getTemplate()).isSameAs(food);
    }

    @Test
    void addQuantity_increasesQuantity() {
        FoodTemplate food = makeFood();
        ItemInstance instance = new ItemInstance(food, 1);
        instance.addQuantity(3);
        assertThat(instance.getQuantity()).isEqualTo(4);
    }

    @Test
    void decreaseQuantity_decreasesQuantity() {
        FoodTemplate food = makeFood();
        ItemInstance instance = new ItemInstance(food, 5);
        instance.decreaseQuantity(2);
        assertThat(instance.getQuantity()).isEqualTo(3);
    }

    @Test
    void decreaseQuantity_clampsAtZero() {
        FoodTemplate food = makeFood();
        ItemInstance instance = new ItemInstance(food, 1);
        instance.decreaseQuantity(5);
        assertThat(instance.getQuantity()).isEqualTo(0);
    }
}
