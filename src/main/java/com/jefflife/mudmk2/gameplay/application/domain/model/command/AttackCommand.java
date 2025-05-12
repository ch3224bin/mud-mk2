package com.jefflife.mudmk2.gameplay.application.domain.model.command;

/**
 * Command for attacking a target in the game.
 * Example: "로보트 때려" (Hit robot)
 */
public record AttackCommand(
    String player,
    String target
) implements Command {
    
    @Override
    public String getPlayer() {
        return player;
    }
}