package com.jefflife.mudmk2.gameplay.application.service.query;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.Monster;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.NonPlayerCharacter;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.PlayerCharacter;
import com.jefflife.mudmk2.gameplay.application.service.required.ActiveMonsterRepository;
import com.jefflife.mudmk2.gameplay.application.service.required.ActiveNpcRepository;
import com.jefflife.mudmk2.gameplay.application.service.required.ActivePlayerRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component
public class DefaultRoomOccupancyQuery implements RoomOccupancyQuery {

    private final ActivePlayerRepository players;
    private final ActiveNpcRepository npcs;
    private final ActiveMonsterRepository monsters;

    public DefaultRoomOccupancyQuery(
            ActivePlayerRepository players,
            ActiveNpcRepository npcs,
            ActiveMonsterRepository monsters
    ) {
        this.players = players;
        this.npcs = npcs;
        this.monsters = monsters;
    }

    @Override
    public List<PlayerCharacter> playersIn(Long roomId) {
        return StreamSupport.stream(players.findAll().spliterator(), false)
                .filter(pc -> pc.getCurrentRoomId().equals(roomId))
                .collect(Collectors.toList());
    }

    @Override
    public List<NonPlayerCharacter> npcsIn(Long roomId) {
        return StreamSupport.stream(npcs.findAll().spliterator(), false)
                .filter(npc -> npc.getCurrentRoomId().equals(roomId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Monster> monstersIn(Long roomId) {
        return StreamSupport.stream(monsters.findAll().spliterator(), false)
                .filter(monster -> monster.isAlive() && monster.getCurrentRoomId().equals(roomId))
                .collect(Collectors.toList());
    }
}
