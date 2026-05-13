package com.jefflife.mudmk2.gameplay.adapter.out.eventpublisher.chat;

import com.jefflife.mudmk2.gameplay.application.service.model.template.ItemInfoVariables;
import com.jefflife.mudmk2.gameplay.application.service.required.SendItemInfoMessagePort;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Component
public class ItemInfoMessageSender implements SendItemInfoMessagePort {

    private final TemplateEngine templateEngine;
    private final ChatEventPublisher chatEventPublisher;

    public ItemInfoMessageSender(final TemplateEngine templateEngine, final ChatEventPublisher chatEventPublisher) {
        this.templateEngine = templateEngine;
        this.chatEventPublisher = chatEventPublisher;
    }

    @Override
    public void sendMessage(final ItemInfoVariables variables) {
        Context context = new Context();
        context.setVariable("name", variables.name());
        context.setVariable("description", variables.description());
        context.setVariable("location", variables.location());
        context.setVariable("typeLabel", variables.typeLabel());
        context.setVariable("weight", variables.weight());
        context.setVariable("quantity", variables.quantity());
        context.setVariable("stackable", variables.stackable());
        context.setVariable("hasRecovery", variables.hasRecovery());
        context.setVariable("hpRecovery", variables.hpRecovery());
        context.setVariable("mpRecovery", variables.mpRecovery());
        context.setVariable("apRecovery", variables.apRecovery());
        context.setVariable("statModifiers", variables.statModifiers());
        context.setVariable("skillRef", variables.skillRef());
        context.setVariable("missionInfo", variables.missionInfo());

        String htmlContent = templateEngine.process("gameplay/item-info", context);
        chatEventPublisher.messageToUser(variables.userId(), htmlContent);
    }
}
