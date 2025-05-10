package com.jefflife.mudmk2.game.application.domain.model.map;

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

	@Column(name = "summary", nullable = false)
	private String summary;

	@Column(name = "description", nullable = false)
	private String description;

	@Column(name = "area_id", nullable = false)
	private long areaId;

	@Embedded
	private WayOuts wayOuts = new WayOuts();

	@Builder
	public Room(final long id, final long areaId, final String summary, final String description, final WayOuts wayOuts) {
		this.id = id;
		this.areaId = areaId;
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

	public void update(String summary, String description) {
		this.summary = summary;
		this.description = description;
	}
}
