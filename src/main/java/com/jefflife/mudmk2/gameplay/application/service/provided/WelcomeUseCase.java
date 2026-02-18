package com.jefflife.mudmk2.gameplay.application.service.provided;

/**
 * Use case for sending welcome messages to users.
 */
public interface WelcomeUseCase {
    /**
     * Sends a welcome message to a user.
     * 
     * @param userId the user ID
     */
    void welcome(Long userId);
}