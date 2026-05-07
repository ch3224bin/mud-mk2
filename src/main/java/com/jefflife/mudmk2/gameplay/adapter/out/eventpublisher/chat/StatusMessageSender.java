package com.jefflife.mudmk2.gameplay.adapter.out.eventpublisher.chat;

import com.jefflife.mudmk2.gameplay.application.service.required.SendStatusMessagePort;
import com.jefflife.mudmk2.gameplay.application.service.model.template.StatusVariables;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

/**
 * Implementation of SendStatusMessagePort that sends status messages to users.
 */
@Component
public class StatusMessageSender implements SendStatusMessagePort {
    private final TemplateEngine templateEngine;
    private final ChatEventPublisher chatEventPublisher;

    public StatusMessageSender(final TemplateEngine templateEngine, final ChatEventPublisher chatEventPublisher) {
        this.templateEngine = templateEngine;
        this.chatEventPublisher = chatEventPublisher;
    }

    @Override
    public void sendMessage(final StatusVariables statusVariables) {
        Context context = new Context();
        context.setVariable("playerName", statusVariables.playerName());
        context.setVariable("characterClass", statusVariables.characterClass());
        context.setVariable("gender", statusVariables.gender());
        context.setVariable("state", statusVariables.state());
        context.setVariable("level", statusVariables.level());
        context.setVariable("experience", statusVariables.experience());
        context.setVariable("nextLevelExp", statusVariables.nextLevelExp());
        context.setVariable("hp", statusVariables.hp());
        context.setVariable("maxHp", statusVariables.maxHp());
        context.setVariable("mp", statusVariables.mp());
        context.setVariable("maxMp", statusVariables.maxMp());
        context.setVariable("ap", statusVariables.ap());
        context.setVariable("maxAp", statusVariables.maxAp());
        context.setVariable("vigor", statusVariables.vigor());
        context.setVariable("physique", statusVariables.physique());
        context.setVariable("agility", statusVariables.agility());
        context.setVariable("intellect", statusVariables.intellect());
        context.setVariable("will", statusVariables.will());
        context.setVariable("meridian", statusVariables.meridian());
        context.setVariable("innerPower", statusVariables.innerPower());
        context.setVariable("specialTechnique", statusVariables.specialTechnique());
        context.setVariable("lightStep", statusVariables.lightStep());
        context.setVariable("fistsAndPalms", statusVariables.fistsAndPalms());
        context.setVariable("swordMethod", statusVariables.swordMethod());
        context.setVariable("bladeMethod", statusVariables.bladeMethod());
        context.setVariable("longWeapon", statusVariables.longWeapon());
        context.setVariable("esotericWeapon", statusVariables.esotericWeapon());
        context.setVariable("archery", statusVariables.archery());
        context.setVariable("roomName", statusVariables.roomName());

        String htmlContent = templateEngine.process("gameplay/status", context);
        chatEventPublisher.messageToUser(statusVariables.userId(), htmlContent);
    }
}