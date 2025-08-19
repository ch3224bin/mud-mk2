package com.jefflife.mudmk2.gamedata.application.service.provided;

import com.jefflife.mudmk2.gamedata.application.service.model.request.LinkRoomRequest;
import com.jefflife.mudmk2.gamedata.application.domain.model.map.Room;
import java.util.List;

public interface RoomLinker {
    List<Room> linkAnotherRoom(LinkRoomRequest linkRoomRequest);
}
