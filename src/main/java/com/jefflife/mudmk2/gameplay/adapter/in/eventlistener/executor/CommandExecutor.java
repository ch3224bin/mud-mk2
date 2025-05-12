package com.jefflife.mudmk2.gameplay.adapter.in.eventlistener.executor;

import com.jefflife.mudmk2.gameplay.application.domain.model.command.Command;

/**
 * Interface for command executors that execute commands.
 */
public interface CommandExecutor {
    /**
     * Checks if this executor can handle the given command.
     *
     * @param command the command to check
     * @return true if this executor can handle the command, false otherwise
     */
    boolean canExecute(Command command);

    /**
     * Executes the given command.
     *
     * @param command the command to execute
     */
    void execute(Command command);
}