package com.jefflife.mudmk2.gameplay.application.domain.model.command;

import java.util.Set;

public enum CommandDictionary {
    ATTACK("공격", "때려", "공"),
    TAKE("줍다", "주워", "집어", "집"),
    DROP("버리다", "버려", "놓다", "버"),
    INVENTORY("소지품", "가방", "인벤"),
    EQUIP("장착", "착용", "입어", "입고", "차다", "차고", "끼다", "껴"),
    UNEQUIP("해제", "벗어", "벗고", "빼다", "빼"),
    EQUIPMENT_VIEW("장비", "장비창");

    private final Set<String> aliases;

    CommandDictionary(String... aliases) {
        this.aliases = Set.of(aliases);
    }

    public String toRegex() {
        return String.join("|", aliases);
    }
}
