package com.jefflife.mudmk2.chat.eventlistener;

import com.jefflife.mudmk2.chat.event.ConvertAndSendToUserEvent;
import com.jefflife.mudmk2.chat.event.SystemNoticeMessageEvent;
import com.jefflife.mudmk2.chat.model.ChatMessage;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class SendMessageEventListener {
    private final SimpMessagingTemplate messagingTemplate;

    public SendMessageEventListener(final SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @EventListener
    public void onMessageReceived(ConvertAndSendToUserEvent event) {
        ChatMessage responseMessage = ChatMessage.builder()
                .sender("System")
                .content(event.message())
                .timestamp(LocalDateTime.now())
                .type(ChatMessage.MessageType.FULL_DESCRIPTION)
                .build();
        messagingTemplate.convertAndSendToUser(event.username(), "/queue/system-messages", responseMessage);
    }

    @EventListener
    public void onSystemNoticeMessageReceived(SystemNoticeMessageEvent event) {
        ChatMessage responseMessage = ChatMessage.builder()
                .sender("System")
                .content(event.message())
                .timestamp(LocalDateTime.now())
                .type(ChatMessage.MessageType.CHAT)
                .build();
        messagingTemplate.convertAndSend("/topic/public", responseMessage);
    }
}
