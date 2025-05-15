package com.jefflife.mudmk2.gameplay.application.domain.model.command;

public record InvalidCommand(
        String player,
        String originalContent
) implements Command {
    @Override
    public String getPlayer() {
        return player;
    }
}

