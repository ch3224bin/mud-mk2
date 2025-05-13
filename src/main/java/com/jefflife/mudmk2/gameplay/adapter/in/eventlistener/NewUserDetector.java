package com.jefflife.mudmk2.gameplay.adapter.in.eventlistener;

import com.jefflife.mudmk2.chat.event.ChatMessageEvent;
import com.jefflife.mudmk2.chat.event.JoinUserEvent;
import com.jefflife.mudmk2.gamedata.application.service.PlayerCharacterService;
import com.jefflife.mudmk2.gameplay.application.service.CharacterCreationService;
import com.jefflife.mudmk2.user.domain.User;
import com.jefflife.mudmk2.user.domain.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
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
    private final UserRepository userRepository;

    public NewUserDetector(
            PlayerCharacterService playerCharacterService,
            CharacterCreationService characterCreationService,
            UserRepository userRepository) {
        this.playerCharacterService = playerCharacterService;
        this.characterCreationService = characterCreationService;
        this.userRepository = userRepository;
    }

    @EventListener
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public boolean createPlayerWhenFirstJoin(JoinUserEvent event) {
        Authentication authentication = (Authentication) event.user();
        String username = event.user().getName();
        String email = "";
        if (authentication.getPrincipal() instanceof final OAuth2User oauth2User) {
            email = oauth2User.getAttribute("email");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("User not found: "));

        if (!playerCharacterService.hasCharacter(user.getId())) {
            logger.info("New user detected: {}", user.getName());

            // Start character creation
            characterCreationService.startCharacterCreation(username, user.getId());
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

        // If the user is already in character creation, process the message
        if (characterCreationService.isInCharacterCreation(username)) {
            return characterCreationService.processMessage(username, event.content());
        }

        // User has a character, let the event propagate
        return false;
    }
}
