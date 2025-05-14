package com.jefflife.mudmk2.user.service;

import com.jefflife.mudmk2.user.domain.User;
import com.jefflife.mudmk2.user.domain.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class UserSessionManager {

    private static final Logger logger = LoggerFactory.getLogger(UserSessionManager.class);
    private static final Map<String, User> connectedUsers = new ConcurrentHashMap<>();
    private final UserRepository userRepository;

    public UserSessionManager(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @EventListener
    public void handleSessionConnected(SessionConnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        Principal principal = accessor.getUser();
        String sessionId = accessor.getSessionId();

        if (principal != null) {
            String mapKey = principal.getName(); // Key for the map
            User user = findUser(principal);

            if (user != null) {
                connectedUsers.put(mapKey, user);
                logger.info("User connected: {} (Principal name: {}). SessionId: {}. Total connected users: {}", user.getId(), mapKey, sessionId, connectedUsers.size());
            } else {
                logger.warn("User object could not be retrieved for principal name: {}. SessionId: {}. User not added to session map.", mapKey, sessionId);
            }
        } else {
            logger.warn("Principal is null in SessionConnectEvent. Cannot track user for session ID: {}", sessionId);
        }
    }

    @EventListener
    public void handleSessionDisconnected(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        Principal principal = accessor.getUser();
        String sessionId = accessor.getSessionId();

        if (principal != null) {
            String mapKey = principal.getName();
            if (connectedUsers.containsKey(mapKey)) {
                User removedUser = connectedUsers.remove(mapKey);
                logger.info("User disconnected: {} (Principal name: {}). SessionId: {}. Total connected users: {}", removedUser.getId(), mapKey, sessionId, connectedUsers.size());
            } else {
                logger.warn("User (Principal name: {}) was not in the connected users map upon disconnect. SessionId: {}", mapKey, sessionId);
            }
        } else {
            logger.warn("Principal is null in SessionDisconnectEvent for session ID: {}. Cannot remove user from map using principal name directly.", sessionId);
        }
    }

    private User findUser(Principal principal) {
        if (principal == null) {
            return null;
        }

        if (principal instanceof final Authentication authentication) {
            if (authentication.getPrincipal() instanceof final OAuth2User oauth2User) {
                String email = oauth2User.getAttribute("email");
                if (email != null) {
                    Optional<User> userOptional = userRepository.findByEmail(email);
                    if (userOptional.isPresent()) {
                        return userOptional.get();
                    } else {
                        logger.warn("OAuth User with email {} (principal name: {}) not found in repository.", email, principal.getName());
                    }
                } else {
                    logger.warn("OAuth2User principal for {} does not have an email attribute. Attempting lookup by principal name as username.", principal.getName());
                }
            }
        }

        logger.warn("User with principal name {} not found by name lookup either.", principal.getName());
        return null;
    }

    public static Optional<User> getConnectedUser(String principalName) {
        return Optional.ofNullable(connectedUsers.get(principalName));
    }

    public static Map<String, User> getAllConnectedUsers() {
        return Map.copyOf(connectedUsers);
    }
}
