package com.jefflife.mudmk2.gameplay.adapter.in.eventlistener;

import com.jefflife.mudmk2.chat.event.JoinUserEvent;
import com.jefflife.mudmk2.gameplay.application.port.in.DisplayRoomInfoUseCase;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
public class DisplayRoomInfoOnJoinListener {
    private final DisplayRoomInfoUseCase displayRoomInfoUseCase;

    public DisplayRoomInfoOnJoinListener(DisplayRoomInfoUseCase displayRoomInfoUseCase) {
        this.displayRoomInfoUseCase = displayRoomInfoUseCase;
    }

    @EventListener
    @Order(3)
    public void handleJoinUserEvent(JoinUserEvent event) {
        displayRoomInfoUseCase.displayRoomInfo(event.user().getName());
    }
}
