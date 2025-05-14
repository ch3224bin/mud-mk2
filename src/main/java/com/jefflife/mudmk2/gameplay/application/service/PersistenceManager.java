package com.jefflife.mudmk2.gameplay.application.service;

import com.jefflife.mudmk2.gamedata.application.domain.model.map.Room;
import com.jefflife.mudmk2.gamedata.application.domain.repository.RoomRepository;
import org.slf4j.Logger;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class PersistenceManager {
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(PersistenceManager.class);

    private final RoomRepository roomRepository;
    private final GameWorldService gameWorldService;

    public PersistenceManager(final RoomRepository roomRepository, final GameWorldService gameWorldService) {
        this.roomRepository = roomRepository;
        this.gameWorldService = gameWorldService;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional(readOnly = true)
    public void loadGameState() {
        // 서버 시작시 DB에서 메모리로 로드
        Iterable<Room> rooms = roomRepository.findAll();
        gameWorldService.loadRooms(rooms);
        logger.info("loadGameState finished");
    }

    @Transactional
    @Scheduled(fixedDelay = 60_000) // 1분마다 저장
    public void persistGameState() {

    }
}
