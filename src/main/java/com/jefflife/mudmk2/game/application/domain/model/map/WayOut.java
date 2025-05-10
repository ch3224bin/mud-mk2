package com.jefflife.mudmk2.game.application.domain.model.map;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
public class WayOut implements Comparable<WayOut> {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@ManyToOne
	@JoinColumn(name = "room_id", nullable = false)
	private Room room;

	@Enumerated(EnumType.STRING)
	@Column(name = "direction", nullable = false)
	private Direction direction;

	@ManyToOne
	@JoinColumn(name = "next_room_id", nullable = false)
	private Room nextRoom;

	@ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.REFRESH})
	@JoinColumn(name = "door_id", nullable = false)
	private Door door;

	@Column(name = "is_show", nullable = false, columnDefinition = "boolean default true")
	private boolean isShow = true;

	@Builder
	public WayOut(long id, Room room, Room nextRoom, Direction direction, Door door, boolean isShow) {
		this.id = id;
		this.room = room;
		this.nextRoom = nextRoom;
		this.direction = direction;
		this.door = door;
		this.isShow = isShow;
	}

	@Override
	public int compareTo(WayOut o) {
		return Integer.compare(this.direction.ordinal(), o.direction.ordinal());
	}

	public String toString() {
		return direction.getName() + (this.getDoor().isLocked() ? "(잠김)" : "");
	}

	public void installDoor(Door door) {
		this.door = door;
	}
}
