package com.jefflife.mudmk2.gameplay.adapter.in.eventlistener.executor;

import com.jefflife.mudmk2.gameplay.application.domain.model.command.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Manages a chain of command executors and delegates execution to them.
 */
@Component
public class CommandExecutorChain {
    private static final Logger logger = LoggerFactory.getLogger(CommandExecutorChain.class);
    private final List<CommandExecutor> executors;

    /**
     * Creates a new CommandExecutorChain with the given executors.
     *
     * @param executors the executors to use
     */
    @Autowired
    public CommandExecutorChain(List<CommandExecutor> executors) {
        this.executors = executors;
    }

    /**
     * Executes the given command by finding an appropriate executor.
     *
     * @param command the command to execute
     * @return true if an executor was found and the command was executed, false otherwise
     */
    public boolean execute(Command command) {
        if (command == null) {
            return false;
        }
        
        for (CommandExecutor executor : executors) {
            if (executor.canExecute(command)) {
                executor.execute(command);
                return true;
            }
        }
        
        logger.warn("No executor found for command: {}", command);
        return false;
    }
}