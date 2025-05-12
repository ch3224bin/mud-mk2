package com.jefflife.mudmk2.gameplay.application.domain.model.command;

/**
 * Command for locking a door in the game.
 * Example: "동 잠궈" (Lock east)
 */
public record LockCommand(
    String player,
    String target
) implements Command {
    
    @Override
    public String getPlayer() {
        return player;
    }
}