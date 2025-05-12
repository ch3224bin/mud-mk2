package com.jefflife.mudmk2.gameplay.application.domain.model.command;

/**
 * Command for checking the player's status in the game.
 * Example: "상태창" (Status window)
 */
public record StatusCommand(
    String player
) implements Command {
    
    @Override
    public String getPlayer() {
        return player;
    }
}