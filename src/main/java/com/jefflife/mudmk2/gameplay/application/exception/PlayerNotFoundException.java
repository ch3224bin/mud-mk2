package com.jefflife.mudmk2.gameplay.application.exception;

import java.util.UUID;

public class PlayerNotFoundException extends RuntimeException {
    public PlayerNotFoundException(Long userId) {
        super("Player not found for userId: " + userId);
    }
    public PlayerNotFoundException(UUID characterId) {
        super("Player not found for characterId: " + characterId);
    }
}
