package com.jefflife.mudmk2.gameplay.adapter.in.eventlistener;

import com.jefflife.mudmk2.chat.event.JoinUserEvent;
import com.jefflife.mudmk2.gameplay.application.port.in.RoomDescriber;
import com.jefflife.mudmk2.user.service.UserSessionManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DisplayRoomInfoOnJoinListener {
    private final RoomDescriber roomDescriber;

    public DisplayRoomInfoOnJoinListener(RoomDescriber roomDescriber) {
        this.roomDescriber = roomDescriber;
    }

    @EventListener
    @Order(3)
    public void handleJoinUserEvent(JoinUserEvent event) {
        UserSessionManager.getConnectedUser(event.getUserName())
                .ifPresentOrElse(
                        user -> roomDescriber.describe(user.getId()),
                        () -> log.warn("User not found: {}", event.getUserName()));
    }
}
