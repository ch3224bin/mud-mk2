package com.jefflife.mudmk2.gameplay.application.service;

import java.util.UUID;

public interface NpcLocationService {
    /**
     * NPC를 지정한 방으로 이동시킨다.
     * @param npcId NPC ID
     * @param roomId 목적지 방 ID
     * @return NPC가 존재하면 true, 없으면 false
     */
    boolean move(UUID npcId, Long roomId);
}
