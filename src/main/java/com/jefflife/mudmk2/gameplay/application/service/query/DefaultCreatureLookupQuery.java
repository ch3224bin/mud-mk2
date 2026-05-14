package com.jefflife.mudmk2.gameplay.application.service.query;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.Monster;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.NonPlayerCharacter;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.PlayerCharacter;
import com.jefflife.mudmk2.gameplay.application.service.required.ActiveMonsterRepository;
import com.jefflife.mudmk2.gameplay.application.service.required.ActiveNpcRepository;
import com.jefflife.mudmk2.gameplay.application.service.required.ActivePlayerRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component
public class DefaultCreatureLookupQuery implements CreatureLookupQuery {

    private final ActivePlayerRepository players;
    private final ActiveNpcRepository npcs;
    private final ActiveMonsterRepository monsters;

    public DefaultCreatureLookupQuery(
            ActivePlayerRepository players,
            ActiveNpcRepository npcs,
            ActiveMonsterRepository monsters
    ) {
        this.players = players;
        this.npcs = npcs;
        this.monsters = monsters;
    }

    @Override
    public Optional<PlayerCharacter> findPlayerByName(String name) {
        return StreamSupport.stream(players.findAll().spliterator(), false)
                .filter(pc -> StringUtils.equalsIgnoreCase(pc.getName(), name))
                .findFirst();
    }

    @Override
    public Optional<NonPlayerCharacter> findNpcByName(String name) {
        return StreamSupport.stream(npcs.findAll().spliterator(), false)
                .filter(npc -> StringUtils.equalsIgnoreCase(npc.getName(), name))
                .findFirst();
    }

    @Override
    public List<Monster> findMonstersByType(Long monsterTypeId) {
        return StreamSupport.stream(monsters.findAll().spliterator(), false)
                .filter(monster -> monster.getMonsterTypeId().equals(monsterTypeId))
                .collect(Collectors.toList());
    }
}
