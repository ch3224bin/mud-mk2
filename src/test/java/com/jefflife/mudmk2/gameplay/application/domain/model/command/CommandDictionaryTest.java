package com.jefflife.mudmk2.gameplay.application.domain.model.command;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class CommandDictionaryTest {

    @Test
    void EQUIP_containsExpectedAliases() {
        String regex = CommandDictionary.EQUIP.toRegex();
        assertThat(regex).contains("장착").contains("입어").contains("끼다").contains("차다");
    }

    @Test
    void UNEQUIP_containsExpectedAliases() {
        String regex = CommandDictionary.UNEQUIP.toRegex();
        assertThat(regex).contains("해제").contains("벗어").contains("빼다");
    }

    @Test
    void EQUIPMENT_VIEW_containsOnlyTwoAliases() {
        String regex = CommandDictionary.EQUIPMENT_VIEW.toRegex();
        assertThat(regex).contains("장비").contains("장비창");
        assertThat(regex).doesNotContain("착용");
    }
}
