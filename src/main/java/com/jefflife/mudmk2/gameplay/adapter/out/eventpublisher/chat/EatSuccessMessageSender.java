package com.jefflife.mudmk2.gameplay.adapter.out.eventpublisher.chat;

import com.jefflife.mudmk2.gameplay.application.service.model.template.EatSuccessVariables;
import com.jefflife.mudmk2.gameplay.application.service.required.SendEatSuccessMessagePort;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Component
public class EatSuccessMessageSender implements SendEatSuccessMessagePort {

    private final TemplateEngine templateEngine;
    private final ChatEventPublisher chatEventPublisher;

    public EatSuccessMessageSender(final TemplateEngine templateEngine, final ChatEventPublisher chatEventPublisher) {
        this.templateEngine = templateEngine;
        this.chatEventPublisher = chatEventPublisher;
    }

    @Override
    public void sendMessage(final EatSuccessVariables variables) {
        Context context = new Context();
        context.setVariable("itemName", variables.itemName());
        context.setVariable("hpRecovery", variables.hpRecovery());
        context.setVariable("mpRecovery", variables.mpRecovery());
        context.setVariable("apRecovery", variables.apRecovery());

        String htmlContent = templateEngine.process("gameplay/eat-success", context);
        chatEventPublisher.messageToUser(variables.userId(), htmlContent);
    }
}
