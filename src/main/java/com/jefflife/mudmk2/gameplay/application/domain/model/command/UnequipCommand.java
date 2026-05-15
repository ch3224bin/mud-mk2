package com.jefflife.mudmk2.gameplay.application.domain.model.command;

public record UnequipCommand(Long userId, String itemName) implements Command {}
