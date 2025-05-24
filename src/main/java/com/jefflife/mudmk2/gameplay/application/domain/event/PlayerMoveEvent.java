package com.jefflife.mudmk2.gameplay.application.domain.event;

import java.util.UUID;

/**
 * 플레이어 이동 이벤트
 */
public record PlayerMoveEvent(UUID characterId, Long fromRoomId, Long toRoomId) {
}
