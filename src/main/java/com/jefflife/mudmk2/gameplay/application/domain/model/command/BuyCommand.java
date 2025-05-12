package com.jefflife.mudmk2.gameplay.application.domain.model.command;

/**
 * Command for buying an item from a merchant in the game.
 * Example: "상인 도시락 사" (Buy lunchbox from merchant)
 */
public record BuyCommand(
    String player,
    String merchant,
    String item
) implements Command {
    
    @Override
    public String getPlayer() {
        return player;
    }
}