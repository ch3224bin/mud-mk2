package com.jefflife.mudmk2.gameplay.application.service;

import com.jefflife.mudmk2.gameplay.application.service.required.ActiveNpcRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class DefaultNpcLocationService implements NpcLocationService {

    private static final Logger logger = LoggerFactory.getLogger(DefaultNpcLocationService.class);
    private final ActiveNpcRepository npcs;

    public DefaultNpcLocationService(ActiveNpcRepository npcs) {
        this.npcs = npcs;
    }

    @Override
    public boolean move(UUID npcId, Long roomId) {
        return npcs.findById(npcId)
                .map(npc -> {
                    npc.moveTo(roomId);
                    logger.debug("Moved NPC {} to room {}", npcId, roomId);
                    return true;
                })
                .orElse(false);
    }
}
