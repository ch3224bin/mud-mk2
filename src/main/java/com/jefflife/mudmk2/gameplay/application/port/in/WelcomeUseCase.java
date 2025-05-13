package com.jefflife.mudmk2.gameplay.application.port.in;

/**
 * Use case for sending welcome messages to users.
 */
public interface WelcomeUseCase {
    /**
     * Sends a welcome message to a user.
     * 
     * @param username the username of the user to welcome
     */
    void welcome(String username);
}