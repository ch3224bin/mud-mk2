package com.jefflife.mudmk2.gameplay.application.service;

import com.jefflife.mudmk2.gamedata.application.domain.model.map.Room;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.NonPlayerCharacter;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.PlayerCharacter;
import com.jefflife.mudmk2.gameplay.application.port.in.DisplayRoomInfoUseCase;
import com.jefflife.mudmk2.gameplay.application.port.out.SendRoomInfoMessagePort;
import com.jefflife.mudmk2.gameplay.application.service.model.template.CreatureInfo;
import com.jefflife.mudmk2.gameplay.application.service.model.template.RoomInfoVariables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DisplayRoomInfoService implements DisplayRoomInfoUseCase {
    private static final Logger logger = LoggerFactory.getLogger(DisplayRoomInfoService.class);

    private final GameWorldService gameWorldService;
    private final SendRoomInfoMessagePort sendRoomInfoMessagePort;

    public DisplayRoomInfoService(
            final GameWorldService gameWorldService,
            final SendRoomInfoMessagePort sendRoomInfoMessagePort
    ) {
        this.gameWorldService = gameWorldService;
        this.sendRoomInfoMessagePort = sendRoomInfoMessagePort;
    }

    @Override
    public void displayRoomInfo(Long userId) {
        final PlayerCharacter character = gameWorldService.getPlayerByUserId(userId);

        if (character == null) {
            logger.info("Player character not found for user {}. Room info will not be displayed.", userId);
            return;
        }

        final Room currentRoom = gameWorldService.getRoom(character.getCurrentRoomId());

        // 현재 방에 있는 NPC 목록 가져오기
        List<CreatureInfo> npcsInRoom = gameWorldService.getNpcsInRoom(currentRoom.getId())
                .stream()
                .map(npc -> new CreatureInfo(npc.getName(), npc.getState()))
                .toList();

        // 현재 방에 있는 다른 플레이어 캐릭터 목록 가져오기
        List<CreatureInfo> otherPlayersInRoom = gameWorldService.getPlayersInRoom(currentRoom.getId())
                .stream()
                .filter(pc -> !pc.getId().equals(character.getId())) // 자신 제외
                .map(pc -> new CreatureInfo(pc.getNickname(), pc.getState()))
                .toList();

        // 현재 방에 있는 몬스터 목록 가져오기
        List<CreatureInfo> monstersInRoom = gameWorldService.getMonstersInRoom(currentRoom.getId())
                .stream()
                .map(monster -> new CreatureInfo(
                        monster.getName() + " (레벨 " + monster.getLevel() + ")",
                        monster.getState()))
                .toList();

        sendRoomInfoMessagePort.sendMessage(new RoomInfoVariables(
                userId,
                currentRoom.getName(),
                currentRoom.getDescription(),
                currentRoom.getExitString(),
                npcsInRoom,
                otherPlayersInRoom,
                monstersInRoom // 몬스터 정보 추가
        ));
        logger.info("Sent room info to user {}", userId);
    }
}
