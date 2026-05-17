package com.jefflife.mudmk2.gameplay.adapter.out.eventpublisher.chat;

import com.jefflife.mudmk2.gameplay.application.service.model.template.MartialArtViewVariables;
import com.jefflife.mudmk2.gameplay.application.service.required.SendMartialArtViewMessagePort;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Component
public class MartialArtViewMessageSender implements SendMartialArtViewMessagePort {

    private final TemplateEngine templateEngine;
    private final ChatEventPublisher chatEventPublisher;

    public MartialArtViewMessageSender(final TemplateEngine templateEngine,
                                       final ChatEventPublisher chatEventPublisher) {
        this.templateEngine = templateEngine;
        this.chatEventPublisher = chatEventPublisher;
    }

    @Override
    public void sendMessage(final MartialArtViewVariables variables) {
        Context context = new Context();
        context.setVariable("mentalGroups", variables.mentalGroups());
        context.setVariable("externalGroups", variables.externalGroups());

        String html = templateEngine.process("gameplay/martial-art-view", context);
        chatEventPublisher.messageToUser(variables.userId(), html);
    }
}
