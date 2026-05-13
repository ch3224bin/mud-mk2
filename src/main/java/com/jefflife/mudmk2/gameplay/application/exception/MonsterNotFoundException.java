package com.jefflife.mudmk2.gameplay.application.exception;

import java.util.UUID;

public class MonsterNotFoundException extends RuntimeException {
    public MonsterNotFoundException(UUID monsterId) {
        super("Monster not found: " + monsterId);
    }
}
