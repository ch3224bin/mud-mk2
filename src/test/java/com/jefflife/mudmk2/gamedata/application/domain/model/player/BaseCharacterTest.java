package com.jefflife.mudmk2.gamedata.application.domain.model.player;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BaseCharacterTest {

    private BaseCharacter makeChar(int hp, int mp, int ap) {
        return BaseCharacter.builder()
                .name("홍길동")
                .hp(hp).mp(mp).ap(ap)
                .build();
    }

    @Test
    void healHp_increasesHpWithinMax() {
        BaseCharacter c = makeChar(50, 0, 0);
        c.healHp(30, 100);
        assertThat(c.getHp()).isEqualTo(80);
    }

    @Test
    void healHp_clampsAtMax() {
        BaseCharacter c = makeChar(50, 0, 0);
        c.healHp(80, 100);
        assertThat(c.getHp()).isEqualTo(100);
    }

    @Test
    void healMp_increasesMpWithinMax() {
        BaseCharacter c = makeChar(0, 20, 0);
        c.healMp(10, 50);
        assertThat(c.getMp()).isEqualTo(30);
    }

    @Test
    void healMp_clampsAtMax() {
        BaseCharacter c = makeChar(0, 20, 0);
        c.healMp(100, 50);
        assertThat(c.getMp()).isEqualTo(50);
    }

    @Test
    void healAp_increasesApWithinMax() {
        BaseCharacter c = makeChar(0, 0, 5);
        c.healAp(10, 40);
        assertThat(c.getAp()).isEqualTo(15);
    }

    @Test
    void healAp_clampsAtMax() {
        BaseCharacter c = makeChar(0, 0, 5);
        c.healAp(100, 40);
        assertThat(c.getAp()).isEqualTo(40);
    }
}
