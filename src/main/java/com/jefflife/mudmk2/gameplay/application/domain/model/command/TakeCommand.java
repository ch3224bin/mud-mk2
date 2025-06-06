package com.jefflife.mudmk2.gameplay.application.domain.model.command;

/**
 * Command for taking an item from a container in the game.
 * Example: "가방 사과 꺼내" (Take apple from bag)
 */
public record TakeCommand(
    Long userId,
    String container,
    String item
) implements Command {
}