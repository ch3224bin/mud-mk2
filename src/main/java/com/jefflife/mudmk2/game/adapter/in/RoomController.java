package com.jefflife.mudmk2.game.adapter.in;

import com.jefflife.mudmk2.game.application.port.in.*;
import com.jefflife.mudmk2.game.application.service.model.request.CreateRoomRequest;
import com.jefflife.mudmk2.game.application.service.model.request.LinkRoomRequest;
import com.jefflife.mudmk2.game.application.service.model.request.UpdateRoomRequest;
import com.jefflife.mudmk2.game.application.service.model.response.LinkedRoomResponse;
import com.jefflife.mudmk2.game.application.service.model.response.RoomResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping(RoomController.BASE_PATH)
public class RoomController {
    public static final String BASE_PATH = "/api/v1/rooms";

    private final CreateRoomUseCase createRoomUseCase;
    private final UpdateRoomUseCase updateRoomUseCase;
    private final GetRoomUseCase getRoomUseCase;
    private final DeleteRoomUseCase deleteRoomUseCase;
    private final LinkedRoomUseCase linkedRoomUseCase;

    public RoomController(
            final CreateRoomUseCase createRoomUseCase,
            final UpdateRoomUseCase updateRoomUseCase,
            final GetRoomUseCase getRoomUseCase,
            final DeleteRoomUseCase deleteRoomUseCase,
            final LinkedRoomUseCase linkedRoomUseCase
    ) {
        this.createRoomUseCase = createRoomUseCase;
        this.updateRoomUseCase = updateRoomUseCase;
        this.getRoomUseCase = getRoomUseCase;
        this.deleteRoomUseCase = deleteRoomUseCase;
        this.linkedRoomUseCase = linkedRoomUseCase;
    }

    @PostMapping
    public ResponseEntity<RoomResponse> createRoom(
            @RequestBody final CreateRoomRequest createRoomRequest
    ) {
        RoomResponse roomResponse = createRoomUseCase.createRoom(createRoomRequest);
        return ResponseEntity
                .created(URI.create(String.format("%s/%s", BASE_PATH, roomResponse.id())))
                .body(roomResponse);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<RoomResponse> updateRoom(
            @PathVariable final Long id,
            @RequestBody final UpdateRoomRequest updateRoomRequest
    ) {
        RoomResponse roomResponse = updateRoomUseCase.updateRoom(id, updateRoomRequest);
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
