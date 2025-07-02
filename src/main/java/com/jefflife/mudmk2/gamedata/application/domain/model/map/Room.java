package com.jefflife.mudmk2.gamedata.application.domain.model.map;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.Optional;

@EqualsAndHashCode(of = "id")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
public class Room {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@Column(name = "name", nullable = false)
	private String name;

	@Column(name = "summary", nullable = false)
	private String summary;

	@Column(name = "description", nullable = false, length = 2000)
	private String description;

	@Column(name = "area_id", nullable = false)
	private long areaId;

	@Embedded
	private WayOuts wayOuts = new WayOuts();

	@Builder
	public Room(final long id, final long areaId, final String name, final String summary, final String description, final WayOuts wayOuts) {
		this.id = id;
		this.areaId = areaId;
		this.name = name;
		this.summary = summary;
		this.description = description;
		this.wayOuts = wayOuts;
	}

	public List<WayOut> getSortedWayOuts() {
		return wayOuts.getSortedWayOuts();
	}

	public String getExitString() {
		return wayOuts.getExitString();
	}

	public Optional<WayOut> getWayOutByDirection(Direction direction) {
		return wayOuts.getWayOutByDirection(direction);
	}

	public WayOut createWayOut(Room nextRoom, Direction direction) {
		WayOut wayout = WayOut.builder()
				.isShow(true)
				.direction(direction)
				.room(this)
				.nextRoom(nextRoom)
				.build();
		wayOuts.add(wayout);
		return wayout;
	}

	public void linkAnotherRoom(Room anotherRoom, Direction myWay, Direction yourWay) {
		WayOut wayOut = this.createWayOut(anotherRoom, myWay);
		linkAnotherRoom(wayOut, anotherRoom, yourWay);
	}

	private void linkAnotherRoom(WayOut wayOut, Room anotherRoom, Direction yourWay) {
		WayOut anotherWayOut = anotherRoom.createWayOut(this, yourWay);
		Door.setup(wayOut, anotherWayOut);
	}

	public void update(RoomUpdateRequest request) {
		this.name = request.name();
		this.summary = request.summary();
		this.description = request.description();
	}

	public String getName() {
	    return this.name;
    }

	public String getSummary() {
		return this.summary;
	}

    public void initializeAssociatedEntities() {
        // WayOuts 컬렉션 및 관련 객체 명시적 초기화
        List<WayOut> actualWayOuts = this.getSortedWayOuts();
        if (actualWayOuts != null) {
            for (WayOut wayOut : actualWayOuts) {
                // WayOut 객체의 기본 속성 접근 (필요시)
                wayOut.getDirection();

                // nextRoom 프록시 초기화
                Room nextRoom = wayOut.getNextRoom();
                if (nextRoom != null) {
                    nextRoom.getId(); // ID 접근으로 프록시 초기화
                    // 필요에 따라 nextRoom.getName(); 등 추가적인 속성 접근 가능
                }
            }
        }
    }

	public Optional<Room> getNextRoomByDirection(final Direction direction) {
		return wayOuts.getWayOutByDirection(direction)
				.map(WayOut::getNextRoom);
	}

	public boolean hasWay(final Direction direction) {
		Optional<WayOut> wayOutByDirection = wayOuts.getWayOutByDirection(direction);
		return wayOutByDirection.isPresent();

	}

	public boolean isLocked(final Direction direction) {
		return wayOuts.isLocked(direction);
	}
}
