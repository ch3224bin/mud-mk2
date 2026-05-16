package com.jefflife.mudmk2.gameplay.adapter.out.eventpublisher.chat;

import com.jefflife.mudmk2.gameplay.application.service.model.template.EquipmentViewVariables;
import com.jefflife.mudmk2.gameplay.application.service.required.SendEquipmentViewMessagePort;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Component
public class EquipmentViewMessageSender implements SendEquipmentViewMessagePort {

    private final TemplateEngine templateEngine;
    private final ChatEventPublisher chatEventPublisher;

    public EquipmentViewMessageSender(final TemplateEngine templateEngine, final ChatEventPublisher chatEventPublisher) {
        this.templateEngine = templateEngine;
        this.chatEventPublisher = chatEventPublisher;
    }

    @Override
    public void sendMessage(final EquipmentViewVariables variables) {
        Context context = new Context();
        context.setVariable("slots", variables.slots());
        context.setVariable("statDiffs", variables.statDiffs());

        String htmlContent = templateEngine.process("gameplay/equipment-view", context);
        chatEventPublisher.messageToUser(variables.userId(), htmlContent);
    }
}
