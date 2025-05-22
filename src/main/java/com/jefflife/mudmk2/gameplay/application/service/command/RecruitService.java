package com.jefflife.mudmk2.gameplay.application.service.command;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.PlayerCharacter;
import com.jefflife.mudmk2.gameplay.application.domain.model.command.RecruitCommand;
import com.jefflife.mudmk2.gameplay.application.port.in.RecruitUseCase;
import com.jefflife.mudmk2.gameplay.application.port.out.SendMessageToUserPort;
import com.jefflife.mudmk2.gameplay.application.service.GameWorldService;
import org.springframework.stereotype.Service;

@Service
public class RecruitService implements RecruitUseCase {
    private final GameWorldService gameWorldService;
    private final SendMessageToUserPort sendMessageToUserPort;

    public RecruitService(final GameWorldService gameWorldService, final SendMessageToUserPort sendMessageToUserPort) {
        this.gameWorldService = gameWorldService;
        this.sendMessageToUserPort = sendMessageToUserPort;
    }

    @Override
    public void recruit(final RecruitCommand recruitCommand) {
        // NPC 대상임
        // target이 같은 방에 있는지 찾고
        // target을 확인하면 합류 발사
        // target은 다른 그룹에 속해 있으면 안된다
        // 합류 후 NPC는 리더를 따라 다녀야함
        // leader player move event를 party 에서 받아서 party member들에게 전달
        // NPC는 리더의 뒤를 무조건 따라가야함 (여러 요인들로 인해 조건을 주면 못따라감)
        // PC의 경우는 별도의 룰을 주어야하는데 나중에 구현

        PlayerCharacter player = gameWorldService.getPlayerByUserId(recruitCommand.userId());

    }
}
