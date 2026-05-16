package com.jefflife.mudmk2.gamedata.application.domain.model.item.effect;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.PlayerCharacter;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class MpRestoreEffectTest {
    @Test
    void applyTo_callsPlayerHealWithMpOnly() {
        PlayerCharacter player = mock(PlayerCharacter.class);
        new MpRestoreEffect(15).applyTo(player);
        verify(player).heal(0, 15, 0);
    }
}
