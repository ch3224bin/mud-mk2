package com.jefflife.mudmk2.gamedata.adapter.webapi;

import com.jefflife.mudmk2.gamedata.application.domain.model.map.RoomRegisterRequest;
import com.jefflife.mudmk2.gamedata.application.domain.model.map.RoomUpdateRequest;
import com.jefflife.mudmk2.gamedata.application.service.model.request.LinkRoomRequest;
import com.jefflife.mudmk2.gamedata.application.service.model.response.LinkedRoomResponse;
import com.jefflife.mudmk2.gamedata.application.service.model.response.RoomResponse;
import com.jefflife.mudmk2.gamedata.application.service.provided.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping(RoomController.BASE_PATH)
public class RoomController {
    public static final String BASE_PATH = "/api/v1/rooms";

    private final RoomCreator roomCreator;
    private final RoomUpdater roomUpdater;
    private final RoomFinder roomFinder;
    private final RoomRemover roomRemover;
    private final RoomLinker roomLinker;

    public RoomController(
            final RoomCreator roomCreator,
            final RoomUpdater roomUpdater,
            final RoomFinder roomFinder,
            final RoomRemover roomRemover,
            final RoomLinker roomLinker
    ) {
        this.roomCreator = roomCreator;
        this.roomUpdater = roomUpdater;
        this.roomFinder = roomFinder;
        this.roomRemover = roomRemover;
        this.roomLinker = roomLinker;
    }

    @PostMapping
    public ResponseEntity<RoomResponse> createRoom(
            @RequestBody final RoomRegisterRequest roomRegisterRequest
    ) {
        RoomResponse roomResponse = RoomResponse.of(roomCreator.register(roomRegisterRequest));
        return ResponseEntity
                .created(URI.create(String.format("%s/%s", BASE_PATH, roomResponse.id())))
                .body(roomResponse);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<RoomResponse> updateRoom(
            @PathVariable final Long id,
            @RequestBody final RoomUpdateRequest roomUpdateRequest
    ) {
        RoomResponse roomResponse = RoomResponse.of(roomUpdater.update(id, roomUpdateRequest));
        return ResponseEntity.ok(roomResponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoomResponse> getRoom(@PathVariable final Long id) {
        return ResponseEntity.ok(roomFinder.getRoom(id));
    }

    @GetMapping
    public ResponseEntity<Page<RoomResponse>> getRooms(final Pageable pageable, @RequestParam(value = "areaId") final Long areaId) {
        return ResponseEntity.ok(roomFinder.getPagedRooms(pageable, areaId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoom(@PathVariable final Long id) {
        roomRemover.deleteRoom(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/link")
    public ResponseEntity<LinkedRoomResponse> linkRooms(
            @RequestBody final LinkRoomRequest linkRoomRequest
    ) {
        LinkedRoomResponse linkedRoomResponse = roomLinker.linkAnotherRoom(linkRoomRequest);
        return ResponseEntity.ok(linkedRoomResponse);
    }

}
