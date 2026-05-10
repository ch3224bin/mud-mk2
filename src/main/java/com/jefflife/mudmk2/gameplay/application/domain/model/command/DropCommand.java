package com.jefflife.mudmk2.gameplay.application.domain.model.command;

public record DropCommand(
    Long userId,
    String itemName,
    int index
) implements Command {}
