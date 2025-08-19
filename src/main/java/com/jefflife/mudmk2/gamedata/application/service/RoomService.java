package com.jefflife.mudmk2.gamedata.application.service;

import com.jefflife.mudmk2.gamedata.application.domain.model.map.Room;
import com.jefflife.mudmk2.gamedata.application.domain.model.map.RoomUpdateRequest;
import com.jefflife.mudmk2.gamedata.application.service.provided.*;
import com.jefflife.mudmk2.gamedata.application.service.required.RoomRepository;
import com.jefflife.mudmk2.gamedata.application.domain.model.map.RoomRegisterRequest;
import com.jefflife.mudmk2.gamedata.application.service.model.request.LinkRoomRequest;
import org.springframework.data.domain.Page;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Validated
@Service
public class RoomService implements RoomFinder, RoomUpdater, RoomCreator, RoomRemover, RoomLinker {

	private final RoomRepository roomRepository;

	public RoomService(final RoomRepository roomRepository) {
		this.roomRepository = roomRepository;
	}

	@Transactional(readOnly = true)
	@Override
	public Room getRoom(final long id) {
		return roomRepository.findById(id)
				.orElseThrow(IllegalArgumentException::new);
	}

	@Transactional(readOnly = true)
	@Override
	public Page<Room> getPagedRooms(final Pageable pageable, final long areaId) {
		return roomRepository.findByAreaId(pageable, areaId);
	}

	@Transactional
	@Override
	public Room update(long id, RoomUpdateRequest roomUpdateRequest) {
		Room room = roomRepository.findById(id)
				.orElseThrow(IllegalArgumentException::new);
		room.update(roomUpdateRequest);
		return roomRepository.save(room);
	}

	@Transactional
	@Override
	public Room register(final RoomRegisterRequest roomRegisterRequest) {
		return roomRepository.save(roomRegisterRequest.toDomain());
	}

	@Override
	public void deleteRoom(final long id) {
		final Room room = roomRepository.findById(id)
				.orElseThrow(IllegalArgumentException::new);
		roomRepository.delete(room);
	}

	@Transactional
	@Override
	public List<Room> linkAnotherRoom(LinkRoomRequest linkRoomRequest) {
		final Room room1 = roomRepository.findById(linkRoomRequest.sourceRoomId())
				.orElseThrow(IllegalArgumentException::new);
		final Room room2 = roomRepository.findById(linkRoomRequest.destinationRoomId())
				.orElseThrow(IllegalArgumentException::new);
		room1.linkAnotherRoom(room2, linkRoomRequest.sourceDir(), linkRoomRequest.destinationDir());
		return List.of(room1, room2);
	}
}
