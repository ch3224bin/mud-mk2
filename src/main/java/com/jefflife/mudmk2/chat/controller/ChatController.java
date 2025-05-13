package com.jefflife.mudmk2.chat.controller;

import com.jefflife.mudmk2.chat.event.ChatMessageEvent;
import com.jefflife.mudmk2.chat.event.JoinUserEvent;
import com.jefflife.mudmk2.chat.model.ChatMessage;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Objects;

@Controller
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ApplicationEventPublisher eventPublisher;

    public ChatController(SimpMessagingTemplate messagingTemplate, ApplicationEventPublisher eventPublisher) {
        this.messagingTemplate = messagingTemplate;
        this.eventPublisher = eventPublisher;
    }

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload ChatMessage chatMessage, Principal principal) {
        eventPublisher.publishEvent(ChatMessageEvent.from(principal, chatMessage));
    }

    @MessageMapping("/chat.addUser")
    public void addUser(@Payload ChatMessage chatMessage, SimpMessageHeaderAccessor headerAccessor) {
        // Add username in web socket session
        Objects.requireNonNull(headerAccessor.getSessionAttributes()).put("username", chatMessage.getSender());

        // Set timestamp
        chatMessage.setTimestamp(LocalDateTime.now());

        // Broadcast that new user joined
        messagingTemplate.convertAndSend("/topic/public", chatMessage);
        eventPublisher.publishEvent(new JoinUserEvent(headerAccessor.getUser(), chatMessage));
    }
}
