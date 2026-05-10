package com.jefflife.mudmk2.gameplay.application.domain.model.command;

import java.util.Set;

public enum CommandDictionary {
    ATTACK("공격", "때려", "공"),
    TAKE("줍다", "주워", "집어"),
    DROP("버리다", "버려", "놓다"),
    INVENTORY("소지품", "가방", "인벤");

    private final Set<String> aliases;

    CommandDictionary(String... aliases) {
        this.aliases = Set.of(aliases);
    }

    public String toRegex() {
        return String.join("|", aliases);
    }
}
