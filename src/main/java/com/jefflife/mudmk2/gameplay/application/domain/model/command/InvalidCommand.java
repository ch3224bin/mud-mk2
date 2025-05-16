package com.jefflife.mudmk2.gameplay.application.domain.model.command;

public record InvalidCommand(
        Long userId,
        String originalContent
) implements Command {
}

