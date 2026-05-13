package com.jefflife.mudmk2.gameplay.application.exception;

import java.util.UUID;

public class PartyNotFoundException extends RuntimeException {
    public PartyNotFoundException(UUID partyId) {
        super("Party not found: " + partyId);
    }
}
