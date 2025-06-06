package com.jefflife.mudmk2.gamedata.application.port.in;

import com.jefflife.mudmk2.gamedata.application.service.model.response.RoomResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface GetRoomUseCase {
    RoomResponse getRoom(long id);
    Page<RoomResponse> getPagedRooms(Pageable pageable, long areaId);
}
