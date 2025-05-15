package com.jefflife.mudmk2.gameplay.application.domain.model.command;

/**
 * Command for moving in a direction in the game.
 * Example: "동" (East)
 */
public record MoveCommand(
    String username,
    String direction
) implements Command {
}