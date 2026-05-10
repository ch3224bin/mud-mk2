package com.jefflife.mudmk2.gamedata.application.domain.model.item;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class StatModifierTest {

    @Test
    void statModifier_holdsStatTypeAndValue() {
        StatModifier modifier = new StatModifier(StatType.VIGOR, 10);
        assertThat(modifier.getStatType()).isEqualTo(StatType.VIGOR);
        assertThat(modifier.getValue()).isEqualTo(10);
    }

    @Test
    void statModifier_supportsAllStatTypes() {
        for (StatType type : StatType.values()) {
            StatModifier modifier = new StatModifier(type, 5);
            assertThat(modifier.getStatType()).isEqualTo(type);
        }
    }
}
