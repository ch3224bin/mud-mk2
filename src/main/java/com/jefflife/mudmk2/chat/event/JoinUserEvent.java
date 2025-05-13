package com.jefflife.mudmk2.chat.event;

import com.jefflife.mudmk2.chat.model.ChatMessage;

import java.security.Principal;

public record JoinUserEvent(
        Principal user,
        ChatMessage chatMessage
) {
}
