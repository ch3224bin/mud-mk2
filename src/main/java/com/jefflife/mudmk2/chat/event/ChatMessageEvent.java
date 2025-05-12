package com.jefflife.mudmk2.chat.event;

import com.jefflife.mudmk2.chat.model.ChatMessage;

import java.time.LocalDateTime;

public record ChatMessageEvent(
        String sender,
        String content,
        LocalDateTime timestamp,
        ChatMessage.MessageType type
) {
    public static ChatMessageEvent from(ChatMessage chatMessage) {
        return new ChatMessageEvent(
                chatMessage.getSender(),
                chatMessage.getContent(),
                chatMessage.getTimestamp(),
                chatMessage.getType()
        );
    }
}