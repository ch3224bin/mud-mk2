package com.jefflife.mudmk2.gameplay.application.service;

import com.jefflife.mudmk2.gameplay.application.port.in.InvalidCommandUseCase;
import com.jefflife.mudmk2.gameplay.application.port.out.SendMessageToUserPort;
import org.springframework.stereotype.Service;

@Service
public class InvalidCommandService implements InvalidCommandUseCase {

    private final SendMessageToUserPort sendMessageToUserPort;

    public InvalidCommandService(SendMessageToUserPort sendMessageToUserPort) {
        this.sendMessageToUserPort = sendMessageToUserPort;
    }

    @Override
    public void notifyInvalidCommand(Long userId, String originalCommand) {
        String message = String.format("'%s'은(는) 올바르지 않은 명령어입니다. '도움말' 명령어로 사용 가능한 명령어를 확인하세요.", originalCommand);
        sendMessageToUserPort.messageToUser(userId, message);
    }
}
