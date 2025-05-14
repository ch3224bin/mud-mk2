package com.jefflife.mudmk2.gameplay.application.service;

import com.jefflife.mudmk2.gamedata.application.domain.model.map.Room;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class GameWorldService {
    private final static Logger logger = LoggerFactory.getLogger(GameWorldService.class);

    private final Map<Long, Room> rooms = new ConcurrentHashMap<>();

    @Transactional(readOnly = true)
    public void loadRooms(Iterable<Room> rooms) {
        rooms.forEach(room -> {
            room.initializeAssociatedEntities(); // Room 객체 내부에서 연관 객체 초기화
            this.rooms.put(room.getId(), room);
        });
        logger.info("Loaded {} rooms", this.rooms.size());
    }

    public Optional<Room> getRoom(Long id) {
        return Optional.ofNullable(rooms.get(id));
    }
}

