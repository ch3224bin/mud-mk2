package com.jefflife.mudmk2.gameplay.application.service;

import com.jefflife.mudmk2.gameplay.application.service.required.ActiveNpcRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class DefaultNpcLocationService implements NpcLocationService {

    private final ActiveNpcRepository npcs;

    public DefaultNpcLocationService(ActiveNpcRepository npcs) {
        this.npcs = npcs;
    }

    @Override
    public boolean move(UUID npcId, Long roomId) {
        return npcs.findById(npcId)
                .map(npc -> { npc.moveTo(roomId); return true; })
                .orElse(false);
    }
}
