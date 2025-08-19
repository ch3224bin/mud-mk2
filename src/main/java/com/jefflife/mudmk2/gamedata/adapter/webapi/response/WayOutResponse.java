package com.jefflife.mudmk2.gamedata.adapter.webapi.response;

import com.jefflife.mudmk2.gamedata.application.domain.model.map.Direction;
import com.jefflife.mudmk2.gamedata.application.domain.model.map.WayOut;
import com.jefflife.mudmk2.gamedata.application.domain.model.map.WayOuts;

import java.util.List;

public record WayOutResponse(
        long id,
        long roomId,
        long nextRoomId,
        Direction direction,
        boolean visible,
        DoorResponse door
) {

    public static List<WayOutResponse> of(final WayOuts wayOuts) {
        return wayOuts.getSortedWayOuts()
                .stream()
                .map(WayOutResponse::of)
                .toList();
    }

    public static WayOutResponse of(final WayOut wayOut) {
        return new WayOutResponse(
                wayOut.getId(),
                wayOut.getRoom().getId(),
                wayOut.getNextRoom().getId(),
                wayOut.getDirection(),
                wayOut.isShow(),
                DoorResponse.of(wayOut.getDoor())
        );
    }
}
