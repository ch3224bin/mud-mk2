package com.jefflife.mudmk2.gameplay.application.service.command;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.PlayerCharacter;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.Statable;
import com.jefflife.mudmk2.gameplay.application.domain.model.command.AttackCommand;
import com.jefflife.mudmk2.gameplay.application.port.in.AttackUseCase;
import com.jefflife.mudmk2.gameplay.application.port.out.SendMessageToUserPort;
import com.jefflife.mudmk2.gameplay.application.service.CombatService;
import com.jefflife.mudmk2.gameplay.application.service.GameWorldService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class AttackCommandService implements AttackUseCase {
    private static final Logger logger = LoggerFactory.getLogger(AttackCommandService.class);

    private final GameWorldService gameWorldService;
    private final CombatService combatService;
    private final SendMessageToUserPort sendMessageToUserPort;

    public AttackCommandService(final GameWorldService gameWorldService, final CombatService combatService, final SendMessageToUserPort sendMessageToUserPort) {
        this.gameWorldService = gameWorldService;
        this.combatService = combatService;
        this.sendMessageToUserPort = sendMessageToUserPort;
    }

    @Override
    public void attack(final AttackCommand command) {
        PlayerCharacter player = gameWorldService.getPlayerByUserId(command.userId());
        Long playerRoomId = player.getCurrentRoomId();

        Statable target = getMonsterInRoom(command, playerRoomId);

        if (target == null) {
            target = getNpcInRoom(command, playerRoomId);
        }

        if (target == null) {
            sendMessageToUserPort.messageToUser(command.userId(), "공격할 대상이 없습니다.");
            return;
        }

        if (!target.isNormal()) {
            sendMessageToUserPort.messageToUser(command.userId(), String.format("%s은(는) 공격할 수 없는 상태입니다. (%s)", target.getName(), target.getState()));
            return;
        }

        // 3. Combat 생성하여 등록
        combatService.startCombat(player, target);
        sendAttackNoticeOtherPlayersInRoom(playerRoomId, player, target);
    }

    private Statable getMonsterInRoom(final AttackCommand command, final Long playerRoomId) {
        return gameWorldService.getMonstersInRoom(playerRoomId)
                .stream()
                .filter(monster -> monster.getName().startsWith(command.target()))
                .min(sortNormalStateFirst())
                .orElse(null);
    }

    private Statable getNpcInRoom(final AttackCommand command, final Long playerRoomId) {
        return gameWorldService.getNpcsInRoom(playerRoomId)
                .stream()
                .filter(npc -> npc.getName().startsWith(command.target()))
                .filter(Statable::isAttackableTarget)
                .min(sortNormalStateFirst())
                .orElse(null);
    }

    private static Comparator<Statable> sortNormalStateFirst() {
        return (m1, m2) -> {
            if (m1.isNormal() && !m2.isNormal()) {
                return -1; // m1이 NORMAL이면 먼저 오도록
            } else if (!m1.isNormal() && m2.isNormal()) {
                return 1; // m2가 NORMAL이면 먼저 오도록
            }
            return 0; // 둘 다 NORMAL이거나 둘 다 비정상 상태면 순서 유지
        };
    }

    private void sendAttackNoticeOtherPlayersInRoom(Long playerRoomId, PlayerCharacter player, Statable target) {
        List<PlayerCharacter> playersInRoom = gameWorldService.getPlayersInRoom(playerRoomId);
        String message = String.format("%s이(가) %s을(를) 공격합니다!", player.getName(), target.getName());
        for (PlayerCharacter playerInRoom : playersInRoom) {
            sendMessageToUserPort.messageToUser(playerInRoom.getUserId(), message);
        }
    }
}
