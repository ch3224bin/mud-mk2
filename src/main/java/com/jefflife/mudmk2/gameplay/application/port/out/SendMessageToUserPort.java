package com.jefflife.mudmk2.gameplay.application.port.out;

public interface SendMessageToUserPort {
    void messageToUser(Long userId, String content);
}
