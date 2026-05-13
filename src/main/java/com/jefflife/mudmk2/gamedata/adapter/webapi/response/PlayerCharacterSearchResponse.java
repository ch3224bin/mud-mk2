package com.jefflife.mudmk2.gamedata.adapter.webapi.response;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.PlayerCharacter;

import java.util.UUID;

public record PlayerCharacterSearchResponse(UUID id, String nickname) {
    public static PlayerCharacterSearchResponse from(final PlayerCharacter character) {
        return new PlayerCharacterSearchResponse(character.getId(), character.getNickname());
    }
}
