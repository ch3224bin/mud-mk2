package com.jefflife.mudmk2.gameplay.application.exception;

import java.util.UUID;

public class NpcNotFoundException extends RuntimeException {
    public NpcNotFoundException(UUID npcId) {
        super("NPC not found: " + npcId);
    }
}
