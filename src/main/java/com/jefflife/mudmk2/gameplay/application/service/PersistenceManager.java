package com.jefflife.mudmk2.gameplay.application.service;

import com.jefflife.mudmk2.gamedata.application.domain.model.map.Room;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.PlayerCharacter;
import com.jefflife.mudmk2.gamedata.application.domain.repository.PlayerCharacterRepository;
import com.jefflife.mudmk2.gamedata.application.domain.repository.RoomRepository;
import com.jefflife.mudmk2.gamedata.application.event.PlayerCharacterCreatedEvent;
import org.slf4j.Logger;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class PersistenceManager {
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(PersistenceManager.class);

    private final PlayerCharacterRepository playerCharacterRepository;
    private final RoomRepository roomRepository;
    private final GameWorldService gameWorldService;

    public PersistenceManager(
            final PlayerCharacterRepository playerCharacterRepository,
            final RoomRepository roomRepository,
            final GameWorldService gameWorldService
    ) {
        this.playerCharacterRepository = playerCharacterRepository;
        this.roomRepository = roomRepository;
        this.gameWorldService = gameWorldService;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional(readOnly = true)
    public void loadGameState() {
        // 서버 시작시 DB에서 메모리로 로드
        Iterable<Room> rooms = roomRepository.findAll();
        gameWorldService.loadRooms(rooms);

        Iterable<PlayerCharacter> players = playerCharacterRepository.findAll();
        gameWorldService.loadPlayers(players);

        logger.info("loadGameState finished");
    }

    /**
     * 플레이어 캐릭터 생성 이벤트 처리
     * 새로 생성된 캐릭터를 GameWorldService의 인메모리 캐시에 추가합니다.
     */
    @EventListener
    public void handlePlayerCharacterCreatedEvent(PlayerCharacterCreatedEvent event) {
        final PlayerCharacter playerCharacter = event.getPlayerCharacter();
        playerCharacter.initializeAssociatedEntities(); // 연관 객체 초기화
        gameWorldService.addPlayer(playerCharacter);
        logger.info("New player character added to game world: {}", playerCharacter.getNickname());
    }

    @Transactional
    @Scheduled(fixedDelay = 60_000) // 1분마다 저장
    public void persistGameState() {
        playerCharacterRepository.saveAll(gameWorldService.getActivePlayers());
    }
}
