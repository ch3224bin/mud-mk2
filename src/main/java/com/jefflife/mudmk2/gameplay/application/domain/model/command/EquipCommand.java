package com.jefflife.mudmk2.gameplay.application.domain.model.command;

public record EquipCommand(
        Long userId,
        String itemName,
        int index
) implements Command {}
