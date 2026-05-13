package com.jefflife.mudmk2.gameplay.application.service.query;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.Monster;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.NonPlayerCharacter;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.PlayerCharacter;

import java.util.List;

public interface RoomOccupancyQuery {
    List<PlayerCharacter>    playersIn(Long roomId);
    List<NonPlayerCharacter> npcsIn(Long roomId);
    List<Monster>            monstersIn(Long roomId);
}
