package com.jefflife.mudmk2.gameplay.adapter.in.eventlistener;

import com.jefflife.mudmk2.chat.event.JoinUserEvent;
import com.jefflife.mudmk2.gameplay.application.port.in.WelcomeUseCase;
import org.springframework.context.event.EventListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
public class WelcomeEventListener {

    private final WelcomeUseCase welcomeUseCase;

    public WelcomeEventListener(WelcomeUseCase welcomeUseCase) {
        this.welcomeUseCase = welcomeUseCase;
    }

    @EventListener
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public void welcome(JoinUserEvent event) {
        String username = event.user().getName();
        welcomeUseCase.welcome(username);
    }
}
