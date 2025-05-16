package com.jefflife.mudmk2.gameplay.application.domain.model.command;

/**
 * Command for attacking a target in the game.
 * Example: "로보트 때려" (Hit robot)
 */
public record AttackCommand(
    Long userId,
    String target
) implements Command {
}