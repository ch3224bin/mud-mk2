package com.jefflife.mudmk2.gameplay.adapter.out.eventpublisher;

import com.jefflife.mudmk2.chat.event.ConvertAndSendToUserEvent;
import com.jefflife.mudmk2.gameplay.application.port.out.SendMessageToUserPort;
import com.jefflife.mudmk2.user.service.UserSessionManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ChatEventPublisher implements SendMessageToUserPort {
    private final ApplicationEventPublisher eventPublisher;

    public ChatEventPublisher(final ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public void messageToUser(Long userId, String content) {
        UserSessionManager.getPrincipalName(userId)
                .ifPresentOrElse(
                        username -> eventPublisher.publishEvent(new ConvertAndSendToUserEvent(username, content)),
                        () -> log.warn("User not found for ID: {}", userId)
                );

    }
}
