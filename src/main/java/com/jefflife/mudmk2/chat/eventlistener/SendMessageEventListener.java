package com.jefflife.mudmk2.chat.eventlistener;

import com.jefflife.mudmk2.chat.event.ConvertAndSendToUserEvent;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class SendMessageEventListener {
    private final SimpMessagingTemplate messagingTemplate;

    public SendMessageEventListener(final SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @EventListener
    public void onMessageReceived(ConvertAndSendToUserEvent event) {
        messagingTemplate.convertAndSendToUser(event.username(), "/queue/system-messages", event.message());
    }
}
