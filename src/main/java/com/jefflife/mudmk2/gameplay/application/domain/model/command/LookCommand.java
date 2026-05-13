package com.jefflife.mudmk2.gameplay.application.domain.model.command;

/**
 * Command for looking in a direction or at an object in the game.
 * Examples: "봐" (look around), "동 봐", "철검 봐", "철검 2 봐"
 */
public record LookCommand(
    Long userId,
    String target,
    int index
) implements Command {
}
