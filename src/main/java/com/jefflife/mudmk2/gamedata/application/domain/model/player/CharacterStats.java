package com.jefflife.mudmk2.gamedata.application.domain.model.player;

public record CharacterStats(
        int hp,
        int maxHp,
        int mp,
        int maxMp,
        int str,
        int dex,
        int con,
        int intelligence,
        int pow,
        int cha
) {
    public int getDexterityModifier() {
        return (dex - 10) / 2;
    }
}
