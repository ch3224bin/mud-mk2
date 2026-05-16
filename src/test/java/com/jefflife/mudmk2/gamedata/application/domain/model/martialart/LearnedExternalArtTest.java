package com.jefflife.mudmk2.gamedata.application.domain.model.martialart;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class LearnedExternalArtTest {

    @Test
    void create_startsAtLevel1Exp0() {
        UUID pcId = UUID.randomUUID();
        LearnedExternalArt l = LearnedExternalArt.create(pcId, 17L);

        assertThat(l.getPlayerCharacterId()).isEqualTo(pcId);
        assertThat(l.getExternalArtTemplateId()).isEqualTo(17L);
        assertThat(l.getCurrentLevel()).isEqualTo(1);
        assertThat(l.getCurrentExp()).isEqualTo(0L);
    }
}
