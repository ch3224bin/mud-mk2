package com.jefflife.mudmk2.gamedata.application.service;

import com.jefflife.mudmk2.gamedata.application.domain.model.map.Room;
import com.jefflife.mudmk2.gamedata.application.domain.repository.RoomRepository;
import com.jefflife.mudmk2.gamedata.application.port.in.*;
import com.jefflife.mudmk2.gamedata.application.service.model.request.CreateRoomRequest;
import com.jefflife.mudmk2.gamedata.application.service.model.request.LinkRoomRequest;
import com.jefflife.mudmk2.gamedata.application.service.model.request.UpdateRoomRequest;
import com.jefflife.mudmk2.gamedata.application.service.model.response.LinkedRoomResponse;
import com.jefflife.mudmk2.gamedata.application.service.model.response.RoomResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RoomService implements GetRoomUseCase, UpdateRoomUseCase, CreateRoomUseCase, DeleteRoomUseCase, LinkedRoomUseCase {

	private final RoomRepository roomRepository;

	public RoomService(final RoomRepository roomRepository) {
		this.roomRepository = roomRepository;
	}

	@Transactional(readOnly = true)
	@Override
	public RoomResponse getRoom(final long id) {
		final Room room = roomRepository.findById(id)
				.orElseThrow(IllegalArgumentException::new);
		return RoomResponse.of(room);
	}

	@Transactional(readOnly = true)
	@Override
	public Page<RoomResponse> getPagedRooms(final Pageable pageable, final long areaId) {
		return roomRepository.findByAreaId(pageable, areaId)
				.map(RoomResponse::of);
	}

	@Transactional
	@Override
	public RoomResponse updateRoom(final long id, final UpdateRoomRequest updateRoomRequest) {
		final Room room = roomRepository.findById(id)
				.orElseThrow(IllegalArgumentException::new);
		room.update(updateRoomRequest.name(), updateRoomRequest.summary(), updateRoomRequest.description());
		roomRepository.save(room);
		return RoomResponse.of(room);
	}

	@Transactional
	@Override
	public RoomResponse createRoom(final CreateRoomRequest createRoomRequest) {
		return RoomResponse.of(roomRepository.save(createRoomRequest.toDomain()));
	}

	@Override
	public void deleteRoom(final long id) {
		final Room room = roomRepository.findById(id)
				.orElseThrow(IllegalArgumentException::new);
		roomRepository.delete(room);
	}

	@Transactional
	@Override
	public LinkedRoomResponse linkAnotherRoom(LinkRoomRequest linkRoomRequest) {
		final Room room1 = roomRepository.findById(linkRoomRequest.sourceRoomId())
				.orElseThrow(IllegalArgumentException::new);
		final Room room2 = roomRepository.findById(linkRoomRequest.destinationRoomId())
				.orElseThrow(IllegalArgumentException::new);
		room1.linkAnotherRoom(room2, linkRoomRequest.sourceDir(), linkRoomRequest.destinationDir());
		return LinkedRoomResponse.of(room1, room2);
	}
}
