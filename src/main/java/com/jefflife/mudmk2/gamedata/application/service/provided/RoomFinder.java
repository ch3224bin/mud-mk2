package com.jefflife.mudmk2.gamedata.application.service.provided;

import com.jefflife.mudmk2.gamedata.application.domain.model.map.Room;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RoomFinder {
    Room getRoom(long id);
    Page<Room> getPagedRooms(Pageable pageable, long areaId);
}
