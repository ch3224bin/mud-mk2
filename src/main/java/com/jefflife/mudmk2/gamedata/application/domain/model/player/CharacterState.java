package com.jefflife.mudmk2.gamedata.application.domain.model.player;

public enum CharacterState {
    NORMAL("평상시"),
    COMBAT("전투중"),
    DEAD("죽음");

    private final String description;
    CharacterState(String description) {
        this.description = description;
    }

    public String description() {
        return description;
    }
}
