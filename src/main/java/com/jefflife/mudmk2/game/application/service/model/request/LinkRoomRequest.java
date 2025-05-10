package com.jefflife.mudmk2.game.application.service.model.request;

import com.jefflife.mudmk2.game.application.domain.model.map.Direction;

public record LinkRoomRequest(Long sourceRoomId, Long destinationRoomId, Direction sourceDir, Direction destinationDir) {
}
