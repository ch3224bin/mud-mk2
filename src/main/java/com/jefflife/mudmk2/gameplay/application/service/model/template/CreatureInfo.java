package com.jefflife.mudmk2.gameplay.application.service.model.template;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.CharacterState;

/**
 * Represents information about a creature (PC, NPC, Monster) to be displayed in the UI.
 */
public record CreatureInfo(
        String name,
        CharacterState state
) {
    /**
     * Returns a description of the creature based on its state.
     * @return A description string
     */
    public String getDescription() {
        return switch (state) {
            case DEAD -> name + "의 시체가 있습니다";
            case COMBAT -> name + "이 싸우고 있습니다";
            case NORMAL -> name + "이 서있습니다";
        };
    }
}