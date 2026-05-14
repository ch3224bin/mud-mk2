package com.jefflife.mudmk2.gameplay.application.service;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.Monster;
import com.jefflife.mudmk2.gameplay.application.service.required.ActiveMonsterRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class DefaultMonsterRespawnService implements MonsterRespawnService {
    private static final Logger logger = LoggerFactory.getLogger(DefaultMonsterRespawnService.class);

    private final ActiveMonsterRepository monsters;

    public DefaultMonsterRespawnService(ActiveMonsterRepository monsters) {
        this.monsters = monsters;
    }

    @Override
    public int respawnAll() {
        int count = 0;
        for (Monster monster : monsters.findAll()) {
            if (!monster.isAlive() && monster.canRespawn()) {
                monster.respawn();
                count++;
                logger.debug("Monster respawned: {} (ID: {}, Room: {})",
                        monster.getName(), monster.getId(), monster.getCurrentRoomId());
            }
        }
        return count;
    }
}
