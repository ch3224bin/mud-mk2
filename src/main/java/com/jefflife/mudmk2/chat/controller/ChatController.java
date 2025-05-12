package com.jefflife.mudmk2.chat.controller;

import com.jefflife.mudmk2.chat.event.ChatMessageEvent;
import com.jefflife.mudmk2.chat.model.ChatMessage;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.time.LocalDateTime;

@Controller
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ApplicationEventPublisher eventPublisher;

    public ChatController(SimpMessagingTemplate messagingTemplate, ApplicationEventPublisher eventPublisher) {
        this.messagingTemplate = messagingTemplate;
        this.eventPublisher = eventPublisher;
    }

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload ChatMessage chatMessage, Principal principal, SimpMessageHeaderAccessor headerAccessor) {
        // Set timestamp if not already set
        if (chatMessage.getTimestamp() == null) {
            chatMessage.setTimestamp(LocalDateTime.now());
        }

        // Get the username from session attributes
        String username = principal.getName();

        // Send the original message to all subscribers (public channel)
        messagingTemplate.convertAndSend("/topic/public", chatMessage);

        // Publish event for the chat message
        eventPublisher.publishEvent(ChatMessageEvent.from(chatMessage));

        // Create a response message only for the sender
        ChatMessage responseMessage = ChatMessage.builder()
                .sender("System")
                .content("hello~")
                .timestamp(LocalDateTime.now())
                .type(ChatMessage.MessageType.CHAT)
                .build();

        // Send the response message only to the sender using their username
        messagingTemplate.convertAndSendToUser(username, "/queue/system-messages", responseMessage);
    }

    @MessageMapping("/chat.addUser")
    public void addUser(@Payload ChatMessage chatMessage, SimpMessageHeaderAccessor headerAccessor) {
        // Add username in web socket session
        headerAccessor.getSessionAttributes().put("username", chatMessage.getSender());

        // Set timestamp
        chatMessage.setTimestamp(LocalDateTime.now());

        // Broadcast that new user joined
        messagingTemplate.convertAndSend("/topic/public", chatMessage);
    }
}
