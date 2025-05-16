package com.jefflife.mudmk2.gameplay.adapter.in.eventlistener;

import com.jefflife.mudmk2.chat.event.JoinUserEvent;
import com.jefflife.mudmk2.gameplay.application.port.in.WelcomeUseCase;
import com.jefflife.mudmk2.user.domain.User;
import com.jefflife.mudmk2.user.service.UserSessionManager;
import org.springframework.context.event.EventListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
        User user = UserSessionManager.getConnectedUser(event.getUserName())
            .orElseThrow(() -> new UsernameNotFoundException(event.getUserName()));
        welcomeUseCase.welcome(user.getId());
    }
}
