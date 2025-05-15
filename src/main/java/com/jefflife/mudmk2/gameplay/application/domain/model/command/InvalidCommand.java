package com.jefflife.mudmk2.gameplay.application.domain.model.command;

public record InvalidCommand(
        String username,
        String originalContent
) implements Command {
}

