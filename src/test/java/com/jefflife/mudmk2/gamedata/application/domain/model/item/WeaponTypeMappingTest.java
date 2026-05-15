package com.jefflife.mudmk2.gamedata.application.domain.model.item;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class WeaponTypeMappingTest {

    @Test
    void weaponSkillFor_mapsAllWeaponTypes() {
        assertThat(WeaponTypeMapping.weaponSkillFor(WeaponType.SWORD)).isEqualTo(StatType.SWORD_METHOD);
        assertThat(WeaponTypeMapping.weaponSkillFor(WeaponType.BLADE)).isEqualTo(StatType.BLADE_METHOD);
        assertThat(WeaponTypeMapping.weaponSkillFor(WeaponType.FIST)).isEqualTo(StatType.FISTS_AND_PALMS);
        assertThat(WeaponTypeMapping.weaponSkillFor(WeaponType.ARCHERY)).isEqualTo(StatType.ARCHERY);
        assertThat(WeaponTypeMapping.weaponSkillFor(WeaponType.ESOTERIC)).isEqualTo(StatType.ESOTERIC_WEAPON);
        assertThat(WeaponTypeMapping.weaponSkillFor(WeaponType.LONG_WEAPON)).isEqualTo(StatType.LONG_WEAPON);
    }

    @Test
    void attackVerb_returnsKoreanVerbPerWeaponType() {
        assertThat(WeaponTypeMapping.attackVerb(WeaponType.SWORD)).isEqualTo("베었다");
        assertThat(WeaponTypeMapping.attackVerb(WeaponType.BLADE)).isEqualTo("내려쳤다");
        assertThat(WeaponTypeMapping.attackVerb(WeaponType.FIST)).isEqualTo("내질렀다");
        assertThat(WeaponTypeMapping.attackVerb(WeaponType.ARCHERY)).isEqualTo("쐈다");
        assertThat(WeaponTypeMapping.attackVerb(WeaponType.LONG_WEAPON)).isEqualTo("휘둘렀다");
        assertThat(WeaponTypeMapping.attackVerb(WeaponType.ESOTERIC)).isEqualTo("휘둘렀다");
    }
}
