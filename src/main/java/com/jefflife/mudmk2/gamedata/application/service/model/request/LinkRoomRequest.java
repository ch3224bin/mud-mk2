package com.jefflife.mudmk2.gamedata.application.service.model.request;

import com.jefflife.mudmk2.gamedata.application.domain.model.map.Direction;

public record LinkRoomRequest(Long sourceRoomId, Long destinationRoomId, Direction sourceDir, Direction destinationDir) {
}
