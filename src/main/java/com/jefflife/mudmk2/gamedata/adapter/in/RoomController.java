package com.jefflife.mudmk2.gamedata.adapter.in;

import com.jefflife.mudmk2.gamedata.application.port.in.*;
import com.jefflife.mudmk2.gamedata.application.domain.model.map.RoomRegisterRequest;
import com.jefflife.mudmk2.gamedata.application.service.model.request.LinkRoomRequest;
import com.jefflife.mudmk2.gamedata.application.domain.model.map.RoomUpdateRequest;
import com.jefflife.mudmk2.gamedata.application.service.model.response.LinkedRoomResponse;
import com.jefflife.mudmk2.gamedata.application.service.model.response.RoomResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping(RoomController.BASE_PATH)
public class RoomController {
    public static final String BASE_PATH = "/api/v1/rooms";

    private final RoomRegister roomRegister;
    private final RoomUpdater roomUpdater;
    private final GetRoomUseCase getRoomUseCase;
    private final DeleteRoomUseCase deleteRoomUseCase;
    private final LinkedRoomUseCase linkedRoomUseCase;

    public RoomController(
            final RoomRegister roomRegister,
            final RoomUpdater roomUpdater,
            final GetRoomUseCase getRoomUseCase,
            final DeleteRoomUseCase deleteRoomUseCase,
            final LinkedRoomUseCase linkedRoomUseCase
    ) {
        this.roomRegister = roomRegister;
        this.roomUpdater = roomUpdater;
        this.getRoomUseCase = getRoomUseCase;
        this.deleteRoomUseCase = deleteRoomUseCase;
        this.linkedRoomUseCase = linkedRoomUseCase;
    }

    @PostMapping
    public ResponseEntity<RoomResponse> createRoom(
            @RequestBody final RoomRegisterRequest roomRegisterRequest
    ) {
        RoomResponse roomResponse = roomRegister.register(roomRegisterRequest);
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
        return ResponseEntity.ok(getRoomUseCase.getRoom(id));
    }

    @GetMapping
    public ResponseEntity<Page<RoomResponse>> getRooms(final Pageable pageable, @RequestParam(value = "areaId", required = true) final long areaId) {
        return ResponseEntity.ok(getRoomUseCase.getPagedRooms(pageable, areaId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoom(@PathVariable final Long id) {
        deleteRoomUseCase.deleteRoom(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/link")
    public ResponseEntity<LinkedRoomResponse> linkRooms(
            @RequestBody final LinkRoomRequest linkRoomRequest
    ) {
        LinkedRoomResponse linkedRoomResponse = linkedRoomUseCase.linkAnotherRoom(linkRoomRequest);
        return ResponseEntity.ok(linkedRoomResponse);
    }

}
