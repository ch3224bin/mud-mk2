package com.jefflife.mudmk2.gamedata.application.domain.model.player;

import com.jefflife.mudmk2.gamedata.application.domain.model.item.StatType;
import com.jefflife.mudmk2.gamedata.application.domain.model.martialart.EquippedMartialArts;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PlayerCharacterHealTest {

    private PlayerCharacter makePlayer(int hp, int mp, int ap, int physique, int meridian, int agility) {
        BaseCharacter base = BaseCharacter.builder()
                .name("홍길동")
                .hp(hp).mp(mp).ap(ap)
                .physique(physique)
                .meridian(meridian)
                .agility(agility)
                .build();
        EquippedItems equipped = mock(EquippedItems.class);
        when(equipped.sumStatModifiers()).thenReturn(Map.<StatType, Integer>of());
        Inventory inventory = mock(Inventory.class);
        return new PlayerCharacter(
                java.util.UUID.randomUUID(),
                base,
                null,
                1L,
                "홍길동",
                null,
                false,
                null,
                inventory,
                equipped,
                EquippedMartialArts.create()
        );
    }

    @Test
    void heal_increasesHpMpApWithinMax() {
        // maxHp = physique*10 + specialTechnique*3 = 10*10 + 0 = 100
        // maxMp = meridian*5 + innerPower*3 = 10*5 + 0 = 50
        // maxAp = agility*8 = 5*8 = 40
        PlayerCharacter p = makePlayer(50, 20, 5, 10, 10, 5);

        p.heal(30, 10, 10);

        assertThat(p.getBaseStats().hp()).isEqualTo(80);
        assertThat(p.getBaseStats().mp()).isEqualTo(30);
        assertThat(p.getBaseStats().ap()).isEqualTo(15);
    }

    @Test
    void heal_clampsAtMax() {
        PlayerCharacter p = makePlayer(50, 20, 5, 10, 10, 5);

        p.heal(999, 999, 999);

        assertThat(p.getBaseStats().hp()).isEqualTo(100);
        assertThat(p.getBaseStats().mp()).isEqualTo(50);
        assertThat(p.getBaseStats().ap()).isEqualTo(40);
    }

    @Test
    void heal_zeroAmounts_doNotChangeValues() {
        PlayerCharacter p = makePlayer(50, 20, 5, 10, 10, 5);

        p.heal(0, 0, 0);

        assertThat(p.getBaseStats().hp()).isEqualTo(50);
        assertThat(p.getBaseStats().mp()).isEqualTo(20);
        assertThat(p.getBaseStats().ap()).isEqualTo(5);
    }
}
