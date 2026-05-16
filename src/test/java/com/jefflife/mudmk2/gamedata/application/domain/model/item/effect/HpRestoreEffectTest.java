package com.jefflife.mudmk2.gamedata.application.domain.model.item.effect;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.PlayerCharacter;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class HpRestoreEffectTest {
    @Test
    void applyTo_callsPlayerHealWithHpOnly() {
        PlayerCharacter player = mock(PlayerCharacter.class);
        new HpRestoreEffect(30).applyTo(player);
        verify(player).heal(30, 0, 0);
    }
}
