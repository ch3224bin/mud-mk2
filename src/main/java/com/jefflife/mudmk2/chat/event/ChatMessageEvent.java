package com.jefflife.mudmk2.chat.event;

import com.jefflife.mudmk2.chat.model.ChatMessage;

import java.security.Principal;
import java.time.LocalDateTime;

public record ChatMessageEvent(
        Principal user,
        String sender,
        String content,
        LocalDateTime timestamp,
        ChatMessage.MessageType type
) {
    public static ChatMessageEvent from(Principal user, ChatMessage chatMessage) {
        return new ChatMessageEvent(
                user,
                chatMessage.getSender(),
                chatMessage.getContent(),
                chatMessage.getTimestamp(),
                chatMessage.getType()
        );
    }
}