package com.jefflife.mudmk2.gameplay.adapter.out.eventpublisher.chat;

import com.jefflife.mudmk2.gameplay.application.service.required.SendRoomInfoMessagePort;
import com.jefflife.mudmk2.gameplay.application.service.model.template.RoomInfoVariables;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Component
public class RoomInfoMessageSender implements SendRoomInfoMessagePort {
    private final TemplateEngine templateEngine;
    private final ChatEventPublisher chatEventPublisher;

    public RoomInfoMessageSender(final TemplateEngine templateEngine, final ChatEventPublisher chatEventPublisher) {
        this.templateEngine = templateEngine;
        this.chatEventPublisher = chatEventPublisher;
    }

    @Override
    public void sendMessage(final RoomInfoVariables roomInfoVariables) {
        Context context = new Context();
        context.setVariable("roomName", roomInfoVariables.roomName());
        context.setVariable("roomDescription", roomInfoVariables.roomDescription());
        context.setVariable("exits", roomInfoVariables.exits());
        context.setVariable("npcsInRoom", roomInfoVariables.npcsInRoom());
        context.setVariable("otherPlayersInRoom", roomInfoVariables.otherPlayersInRoom());
        context.setVariable("monstersInRoom", roomInfoVariables.monstersInRoom());

        String htmlContent = templateEngine.process("gameplay/room-info", context);
        chatEventPublisher.messageToUser(roomInfoVariables.userId(), htmlContent);
    }
}
