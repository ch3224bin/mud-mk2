package com.jefflife.mudmk2.gameplay.adapter.in.eventlistener;

import com.jefflife.mudmk2.chat.event.ChatMessageEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class ChatMessageEventListener {

    private static final Logger logger = LoggerFactory.getLogger(ChatMessageEventListener.class);

    @Async("taskExecutor")
    @EventListener
    public void handleChatMessage(ChatMessageEvent event) {
        // Log the received event
        logger.info("Received chat message event in gameplay module: sender={}, content={}, type={}",
                event.sender(), event.content(), event.type());

        // TODO: Process the chat message for gameplay logic
        // For example, check if it's a command, trigger game actions, etc.
    }
}
