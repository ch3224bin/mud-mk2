package com.jefflife.mudmk2.gameplay.application.domain.model.command;

import java.util.Set;

public enum CommandDictionary {
    ATTACK("공격", "때려", "공");

    private final Set<String> aliases;

    CommandDictionary(String... aliases) {
        this.aliases = Set.of(aliases);
    }

    public String toRegex() {
        return String.join("|", aliases);
    }
}
