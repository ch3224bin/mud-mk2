package com.jefflife.mudmk2.gameplay.application.domain.model.command;

/**
 * Command for attacking a target in the game.
 * Example: "로보트 때려" (Hit robot)
 */
public record AttackCommand(
    String username,
    String target
) implements Command {
}