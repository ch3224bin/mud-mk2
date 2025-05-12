package com.jefflife.mudmk2.gameplay.adapter.out.eventpublisher;

import com.jefflife.mudmk2.chat.event.ConvertAndSendToUserEvent;
import com.jefflife.mudmk2.gameplay.application.port.out.SendMessageToUserPort;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class ChatEventPublisher implements SendMessageToUserPort {
    private final ApplicationEventPublisher eventPublisher;

    public ChatEventPublisher(final ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public void messageToUser(String username, String content) {
        eventPublisher.publishEvent(new ConvertAndSendToUserEvent(username, content));
    }
}
