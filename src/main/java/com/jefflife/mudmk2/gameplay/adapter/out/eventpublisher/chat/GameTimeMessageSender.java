package com.jefflife.mudmk2.gameplay.adapter.out.eventpublisher.chat;

import com.jefflife.mudmk2.gameplay.application.port.out.SendGameTimeMessagePort;
import com.jefflife.mudmk2.gameplay.application.service.model.template.GameTimeVariables;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Component
public class GameTimeMessageSender implements SendGameTimeMessagePort {
    private final TemplateEngine templateEngine;
    private final ChatEventPublisher chatEventPublisher;

    public GameTimeMessageSender(final TemplateEngine templateEngine, final ChatEventPublisher chatEventPublisher) {
        this.templateEngine = templateEngine;
        this.chatEventPublisher = chatEventPublisher;
    }

    @Override
    public void sendMessage(final GameTimeVariables gameTimeVariables) {
        final Context context = new Context();
        context.setVariable("gameHour", gameTimeVariables.gameHour());
        context.setVariable("dayPeriod", gameTimeVariables.dayPeriod());
        chatEventPublisher.sendSystemMessage(templateEngine.process("gameplay/game-time", context));
    }
}
