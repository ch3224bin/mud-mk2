package com.jefflife.mudmk2.gamedata.application.domain.model.item.effect;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.PlayerCharacter;

public record HpRestoreEffect(int amount) implements EatEffect {
    @Override
    public void applyTo(PlayerCharacter player) {
        player.heal(amount, 0, 0);
    }
}
