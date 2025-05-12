package com.jefflife.mudmk2.gameplay.adapter.in.eventlistener;

import com.jefflife.mudmk2.chat.event.ChatMessageEvent;
import com.jefflife.mudmk2.gameplay.adapter.in.eventlistener.executor.CommandExecutorChain;
import com.jefflife.mudmk2.gameplay.adapter.in.eventlistener.parser.CommandParserChain;
import com.jefflife.mudmk2.gameplay.application.domain.model.command.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Listens for chat message events and processes them as game commands.
 * Uses the Chain of Responsibility pattern for command parsing and execution.
 */
@Component
public class ChatMessageEventListener {
    private static final Logger logger = LoggerFactory.getLogger(ChatMessageEventListener.class);
    
    private final CommandParserChain parserChain;
    private final CommandExecutorChain executorChain;

    public ChatMessageEventListener(final CommandParserChain parserChain, final CommandExecutorChain executorChain) {
        this.parserChain = parserChain;
        this.executorChain = executorChain;
    }

    /**
     * Handles chat message events by parsing them into commands and executing them.
     *
     * @param event the chat message event to handle
     */
    @Async("taskExecutor")
    @EventListener
    public void handleChatMessage(ChatMessageEvent event) {
        // Log the received event
        logger.info("Received chat message event in gameplay module: sender={}, content={}, type={}",
                event.sender(), event.content(), event.type());

        try {
            // Parse the message into a command
            Command command = parserChain.parse(event.sender(), event.content());
            
            // Execute the command if it was parsed successfully
            if (command != null) {
                executorChain.execute(command);
            }
        } catch (Exception e) {
            logger.error("Error processing command: {}", e.getMessage(), e);
        }
    }
}