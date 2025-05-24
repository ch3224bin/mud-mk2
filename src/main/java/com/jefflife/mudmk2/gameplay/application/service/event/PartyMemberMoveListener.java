package com.jefflife.mudmk2.gameplay.application.service.event;

import com.jefflife.mudmk2.gamedata.application.domain.model.party.Party;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.NonPlayerCharacter;
import com.jefflife.mudmk2.gameplay.application.domain.event.PlayerMoveEvent;
import com.jefflife.mudmk2.gameplay.application.port.out.SendMessageToUserPort;
import com.jefflife.mudmk2.gameplay.application.service.GameWorldService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * 플레이어 이동 이벤트를 수신하여 NPC 파티 멤버들이 리더를 따라갈 수 있도록 처리하는 리스너
 */
@Component
public class PartyMemberMoveListener {
    private static final Logger logger = LoggerFactory.getLogger(PartyMemberMoveListener.class);

    private final GameWorldService gameWorldService;
    private final SendMessageToUserPort sendMessageToUserPort;

    public PartyMemberMoveListener(
            GameWorldService gameWorldService,
            SendMessageToUserPort sendMessageToUserPort
    ) {
        this.gameWorldService = gameWorldService;
        this.sendMessageToUserPort = sendMessageToUserPort;
    }

    @Async("taskExecutor")
    @EventListener
    public void onApplicationEvent(PlayerMoveEvent event) {
        UUID characterId = event.characterId();
        Long toRoomId = event.toRoomId();

        // 파티가 있는지 확인하고, 리더인지 확인
        Optional<Party> partyOpt = gameWorldService.getPartyByPlayerId(characterId);
        if (partyOpt.isEmpty()) {
            return;
        }

        Party party = partyOpt.get();
        if (!party.isLeader(characterId)) {
            return; // 리더가 아닌 경우 무시
        }

        // 파티 멤버들 중 NPC를 찾아서 리더를 따라가게 함
        for (UUID memberId : party.getMemberIds()) {
            if (memberId.equals(characterId)) {
                continue; // 리더 자신은 건너뜀
            }

            // NPC인지 확인 (캐릭터 ID가 NPC의 ID와 일치하는지 확인)
            NonPlayerCharacter npc = gameWorldService.getNpcById(memberId);
            if (npc != null) {
                // NPC를 이동시키는 로직
                Long npcRoomId = npc.getCurrentRoomId();
                if (!npcRoomId.equals(toRoomId)) {
                    // NPC 이동 로직 (여기서는 단순히 방 ID 변경만 수행)
                    gameWorldService.moveNpcToRoom(npc.getId(), toRoomId);

                    // 로그 추가
                    logger.info("NPC {} followed leader {} to room {}", npc.getName(), characterId, toRoomId);

                    // 파티 리더에게 메시지 전송 (실제 플레이어 ID 조회 필요)
                    String message = npc.getName() + "이(가) 당신을 따라옵니다.";
                    try {
                        Long userId = gameWorldService.getUserIdByCharacterId(characterId);
                        sendMessageToUserPort.messageToUser(userId, message);
                    } catch (Exception e) {
                        logger.warn("Failed to send follow message to leader: {}", e.getMessage());
                    }
                }
            }
        }
    }
}
