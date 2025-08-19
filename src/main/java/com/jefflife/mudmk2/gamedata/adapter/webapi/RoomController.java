package com.jefflife.mudmk2.gamedata.adapter.webapi;

import com.jefflife.mudmk2.gamedata.adapter.webapi.response.LinkedRoomResponse;
import com.jefflife.mudmk2.gamedata.adapter.webapi.response.RoomResponse;
import com.jefflife.mudmk2.gamedata.application.domain.model.map.Room;
import com.jefflife.mudmk2.gamedata.application.domain.model.map.RoomRegisterRequest;
import com.jefflife.mudmk2.gamedata.application.domain.model.map.RoomUpdateRequest;
import com.jefflife.mudmk2.gamedata.application.service.model.request.LinkRoomRequest;
import com.jefflife.mudmk2.gamedata.application.service.provided.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping(RoomController.BASE_PATH)
@RequiredArgsConstructor
public class RoomController {
    public static final String BASE_PATH = "/api/v1/rooms";

    private final RoomCreator roomCreator;
    private final RoomUpdater roomUpdater;
    private final RoomFinder roomFinder;
    private final RoomRemover roomRemover;
    private final RoomLinker roomLinker;

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
        Room room = roomFinder.getRoom(id);
        return ResponseEntity.ok(RoomResponse.of(room));
    }

    @GetMapping
    public ResponseEntity<Page<RoomResponse>> getRooms(final Pageable pageable, @RequestParam(value = "areaId") final Long areaId) {
        Page<Room> roomsPage = roomFinder.getPagedRooms(pageable, areaId);
        return ResponseEntity.ok(roomsPage.map(RoomResponse::of));
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
        List<Room> linkedRooms = roomLinker.linkAnotherRoom(linkRoomRequest);
        LinkedRoomResponse linkedRoomResponse = LinkedRoomResponse.of(linkedRooms.get(0), linkedRooms.get(1));
        return ResponseEntity.ok(linkedRoomResponse);
    }

}
