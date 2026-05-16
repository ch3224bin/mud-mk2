package com.jefflife.mudmk2.gamedata.application.domain.model.item;

import com.jefflife.mudmk2.gamedata.application.domain.model.item.effect.ApRestoreEffect;
import com.jefflife.mudmk2.gamedata.application.domain.model.item.effect.EatEffect;
import com.jefflife.mudmk2.gamedata.application.domain.model.item.effect.HpRestoreEffect;
import com.jefflife.mudmk2.gamedata.application.domain.model.item.effect.MpRestoreEffect;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FoodTemplateTest {

    @Test
    void getEatEffects_includesAllNonZeroRecoveries() {
        FoodTemplate food = FoodTemplate.builder()
                .name("산해진미").description("진수성찬").weight(2).stackable(false)
                .hpRecovery(30).mpRecovery(20).apRecovery(10)
                .build();

        List<EatEffect> effects = food.getEatEffects();

        assertThat(effects).hasSize(3);
        assertThat(effects).contains(
                new HpRestoreEffect(30),
                new MpRestoreEffect(20),
                new ApRestoreEffect(10)
        );
    }

    @Test
    void getEatEffects_excludesZeroRecoveries() {
        FoodTemplate food = FoodTemplate.builder()
                .name("사과").description("빨간 사과").weight(1).stackable(true)
                .hpRecovery(30).mpRecovery(0).apRecovery(0)
                .build();

        List<EatEffect> effects = food.getEatEffects();

        assertThat(effects).containsExactly(new HpRestoreEffect(30));
    }

    @Test
    void getEatEffects_allZero_returnsEmptyList() {
        FoodTemplate food = FoodTemplate.builder()
                .name("물").description("맹물").weight(1).stackable(true)
                .hpRecovery(0).mpRecovery(0).apRecovery(0)
                .build();

        assertThat(food.getEatEffects()).isEmpty();
    }
}
