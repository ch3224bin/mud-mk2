package com.jefflife.mudmk2.game.application.domain.repository;

import com.jefflife.mudmk2.game.application.domain.model.map.Room;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

public interface RoomRepository extends CrudRepository<Room, Long> {
    Page<Room> findAll(Pageable pageable);
}
