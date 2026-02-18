package com.jefflife.mudmk2.gameplay.application.service.command.look;

import com.jefflife.mudmk2.gameplay.application.service.required.SendMessageToUserPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class DirectionDescriber implements LookTargetDescriber {
    private final SendMessageToUserPort sendMessageToUserPort;

    @Override
    public void describe(Long userId, Lookable target) {
        sendMessageToUserPort.messageToUser(userId, String.format("%s이(가) 보인다.\n%s", target.getName(), target.getProperties().get("summary")));
    }

    @Override
    public LookableType getLookableType() {
        return LookableType.DIRECTION;
    }
}
