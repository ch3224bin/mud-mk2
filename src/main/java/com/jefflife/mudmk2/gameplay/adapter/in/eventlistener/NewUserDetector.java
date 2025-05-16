package com.jefflife.mudmk2.gameplay.adapter.in.eventlistener;

import com.jefflife.mudmk2.chat.event.ChatMessageEvent;
import com.jefflife.mudmk2.chat.event.JoinUserEvent;
import com.jefflife.mudmk2.gamedata.application.service.PlayerCharacterService;
import com.jefflife.mudmk2.gameplay.application.service.CharacterCreationService;
import com.jefflife.mudmk2.user.domain.User;
import com.jefflife.mudmk2.user.service.UserSessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

/**
 * Detects new users and starts character creation for them.
 * This listener runs before the ChatMessageEventListener to intercept messages from users in character creation.
 */
@Component
public class NewUserDetector {
    private static final Logger logger = LoggerFactory.getLogger(NewUserDetector.class);

    private final PlayerCharacterService playerCharacterService;
    private final CharacterCreationService characterCreationService;

    public NewUserDetector(
            final PlayerCharacterService playerCharacterService,
            final CharacterCreationService characterCreationService
    ) {
        this.playerCharacterService = playerCharacterService;
        this.characterCreationService = characterCreationService;
    }

    @EventListener
    @Order(1)
    public boolean createPlayerWhenFirstJoin(JoinUserEvent event) {
        String username = event.user().getName();
        User user = UserSessionManager.getConnectedUser(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));

        if (!playerCharacterService.hasCharacter(user.getId())) {
            logger.info("New user detected: {}", user.getId());

            // Start character creation
            characterCreationService.startCharacterCreation(user.getId());
            return true;
        }

        return false;
    }

    /**
     * Listen for chat messages and check if the user needs to create a character.
     * This listener runs before the ChatMessageEventListener to intercept messages from users in character creation.
     * 
     * @param event the chat message event
     * @return true if the event was consumed, false otherwise
     */
    @EventListener
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public boolean handleChatMessage(ChatMessageEvent event) {
        String username = event.user().getName();
        User user = UserSessionManager.getConnectedUser(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));

        // If the user is already in character creation, process the message
        if (characterCreationService.isInCharacterCreation(user.getId())) {
            return characterCreationService.processMessage(user.getId(), event.content());
        }

        // User has a character, let the event propagate
        return false;
    }
}
