package com.jefflife.mudmk2.gamedata.application.service.provided;

import com.jefflife.mudmk2.gamedata.application.service.model.response.RoomResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RoomFinder {
    RoomResponse getRoom(long id);
    Page<RoomResponse> getPagedRooms(Pageable pageable, long areaId);
}
