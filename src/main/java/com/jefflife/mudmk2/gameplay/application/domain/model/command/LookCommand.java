package com.jefflife.mudmk2.gameplay.application.domain.model.command;

/**
 * Command for looking in a direction or at an object in the game.
 * Example: "동 봐" (Look east)
 */
public record LookCommand(
    String username,
    String target
) implements Command {
}