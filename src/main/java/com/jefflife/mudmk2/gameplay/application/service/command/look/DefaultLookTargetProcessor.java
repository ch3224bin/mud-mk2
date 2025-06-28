package com.jefflife.mudmk2.gameplay.application.service.command.look;

import com.jefflife.mudmk2.gameplay.application.port.out.SendMessageToUserPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@RequiredArgsConstructor
@Component
public class DefaultLookTargetProcessor implements LookTargetProcessor {
    private final LookableTargetFinder targetFinder;
    private final DescriberManager describerManager;
    private final SendMessageToUserPort sendMessageToUserPort;
    
    @Override
    public void processLookTarget(Long userId, String targetName) {
        Optional<Lookable> targetOpt = targetFinder.findTargetInRoom(userId, targetName);
        
        if (targetOpt.isEmpty()) {
            sendTargetNotFoundMessage(userId, targetName);
            return;
        }
        
        describerManager.findAndExecute(userId, targetOpt.get());
    }

    private void sendTargetNotFoundMessage(Long userId, String targetName) {
        sendMessageToUserPort.messageToUser(userId, "'" + targetName + "'을(를) 찾을 수 없습니다.");
    }
}