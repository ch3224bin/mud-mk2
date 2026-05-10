package com.jefflife.mudmk2.gameplay.application.domain.model.command;

public record TakeCommand(
    Long userId,
    String itemName,
    int index
) implements Command {}