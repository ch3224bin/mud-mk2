package com.jefflife.mudmk2.gameplay.application.service.required;

public interface SendMessageToUserPort {
    void messageToUser(Long userId, String content);
}
