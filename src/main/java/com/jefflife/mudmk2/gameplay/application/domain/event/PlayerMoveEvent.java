package com.jefflife.mudmk2.gameplay.application.domain.event;

/**
 * 플레이어 이동 이벤트
 */
public record PlayerMoveEvent(Long characterId, Long fromRoomId, Long toRoomId) {
}
