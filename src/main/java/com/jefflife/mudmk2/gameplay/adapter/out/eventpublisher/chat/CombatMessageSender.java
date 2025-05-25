package com.jefflife.mudmk2.gameplay.adapter.out.eventpublisher.chat;

import com.jefflife.mudmk2.gameplay.application.port.out.SendCombatMessagePort;
import com.jefflife.mudmk2.gameplay.application.service.model.template.CombatStartVariables;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Component
public class CombatMessageSender implements SendCombatMessagePort {
    private final TemplateEngine templateEngine;
    private final ChatEventPublisher chatEventPublisher;

    public CombatMessageSender(final TemplateEngine templateEngine, final ChatEventPublisher chatEventPublisher) {
        this.templateEngine = templateEngine;
        this.chatEventPublisher = chatEventPublisher;
    }

    @Override
    public void sendCombatStartMessageToUser(final CombatStartVariables variables) {
        Context context = new Context();
        context.setVariable("initiativeGroup", variables.startResult().initiativeGroup().name());
        context.setVariable("allyInitiative", variables.startResult().allyInitiative());
        context.setVariable("enemyInitiative", variables.startResult().enemyInitiative());

        String htmlContent = templateEngine.process("gameplay/combat-start", context);
        chatEventPublisher.messageToUser(variables.userId(), htmlContent);
    }
}
