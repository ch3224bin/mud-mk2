package com.jefflife.mudmk2.gameplay.application.domain.model.command;

/**
 * Base interface for all command objects.
 * Commands represent player actions in the game.
 */
public interface Command {
    Long userId();
}