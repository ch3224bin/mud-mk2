package com.jefflife.mudmk2.gamedata.application.domain.model.map;

import com.jefflife.mudmk2.gamedata.application.domain.model.item.ItemInstance;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@EqualsAndHashCode(of = "id")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
public class Room {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "name", nullable = false)
	private String name;

	@Column(name = "summary", nullable = false)
	private String summary;

	@Column(name = "description", nullable = false, length = 2000)
	private String description;

	@Column(name = "area_id", nullable = false)
	private Long areaId;

	@Embedded
	private WayOuts wayOuts = new WayOuts();

	@Column(name = "simulation_room", nullable = false)
	private boolean simulationRoom = false;

	@OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
	@JoinColumn(name = "room_id")
	private List<ItemInstance> floorItems = new ArrayList<>();

	@Builder
	public Room(final Long id, final Long areaId, final String name, final String summary, final String description, final WayOuts wayOuts, final boolean simulationRoom) {
		this.id = id;
		this.areaId = areaId;
		this.name = name;
		this.summary = summary;
		this.description = description;
		this.wayOuts = wayOuts;
		this.simulationRoom = simulationRoom;
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

        // floorItems LAZY 컬렉션 강제 초기화 — detached 상태에서 addFloorItem/removeFloorItem 호출 가능하도록
        this.floorItems.size();
        for (ItemInstance item : this.floorItems) {
            item.initializeAssociatedEntities();
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

	public List<ItemInstance> getFloorItems() {
		return Collections.unmodifiableList(floorItems);
	}

	/**
	 * 방 바닥에 아이템을 추가한다. detached 캐시 invariant 를 위해 아이템의 LAZY 그래프를
	 * 강제 초기화한 뒤 컬렉션에 담는다 — 이 메서드를 통과한 아이템은 세션 없이도
	 * template / 하위 그래프에 안전히 접근할 수 있음이 보장된다.
	 */
	public void addFloorItem(ItemInstance item) {
		item.initializeAssociatedEntities();
		this.floorItems.add(item);
	}

	public void removeFloorItem(ItemInstance item) {
		this.floorItems.remove(item);
	}

	public List<ItemInstance> findFloorItemsByName(String name) {
		return floorItems.stream()
				.filter(i -> i.getTemplate().getName().equals(name))
				.toList();
	}
}
