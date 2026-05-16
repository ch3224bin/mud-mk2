package com.jefflife.mudmk2.gamedata.application.domain.model.item.effect;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.PlayerCharacter;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class ApRestoreEffectTest {
    @Test
    void applyTo_callsPlayerHealWithApOnly() {
        PlayerCharacter player = mock(PlayerCharacter.class);
        new ApRestoreEffect(5).applyTo(player);
        verify(player).heal(0, 0, 5);
    }
}
