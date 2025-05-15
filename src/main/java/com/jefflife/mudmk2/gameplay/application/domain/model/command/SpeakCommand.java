package com.jefflife.mudmk2.gameplay.application.domain.model.command;

/**
 * Command for speaking or saying something in the game.
 * Example: "안녕하세요 말" (Say hello)
 */
public record SpeakCommand(
    String username,
    String message
) implements Command {
}