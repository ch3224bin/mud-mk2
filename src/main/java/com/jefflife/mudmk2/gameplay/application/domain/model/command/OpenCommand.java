package com.jefflife.mudmk2.gameplay.application.domain.model.command;

/**
 * Command for opening a door or container in the game.
 * Example: "동 열어" (Open east)
 */
public record OpenCommand(
    String username,
    String target
) implements Command {
}