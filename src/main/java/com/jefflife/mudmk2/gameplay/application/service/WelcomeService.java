package com.jefflife.mudmk2.gameplay.application.service;

import com.jefflife.mudmk2.gameplay.application.port.in.WelcomeUseCase;
import com.jefflife.mudmk2.gameplay.application.port.out.SendMessageToUserPort;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

/**
 * Service for sending welcome messages to users.
 */
@Service
public class WelcomeService implements WelcomeUseCase {

    private final SendMessageToUserPort sendMessageToUserPort;
    private final TemplateEngine templateEngine;
    
    public WelcomeService(
            final SendMessageToUserPort sendMessageToUserPort,
            final TemplateEngine templateEngine
    ) {
        this.sendMessageToUserPort = sendMessageToUserPort;
        this.templateEngine = templateEngine;
    }
    
    @Override
    public void welcome(String username) {
        String welcomeMessage = templateEngine.process("gameplay/welcome", new Context());
        sendMessageToUserPort.messageToUser(username, welcomeMessage);
    }
}