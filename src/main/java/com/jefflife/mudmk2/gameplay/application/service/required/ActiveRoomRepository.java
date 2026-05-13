package com.jefflife.mudmk2.gameplay.application.service.required;

import com.jefflife.mudmk2.gamedata.application.domain.model.map.Room;

import java.util.Optional;

public interface ActiveRoomRepository {
    Optional<Room> findById(Long id);
    Iterable<Room> findAll();
    void add(Room room);
    void remove(Long id);
}
