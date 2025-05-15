package com.jefflife.mudmk2.gameplay.application.domain.model.command;

/**
 * Command for checking the player's status in the game.
 * Example: "상태창" (Status window)
 */
public record StatusCommand(
    String username
) implements Command {
}