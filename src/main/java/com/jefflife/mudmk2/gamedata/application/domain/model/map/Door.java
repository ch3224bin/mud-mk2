package com.jefflife.mudmk2.gamedata.application.domain.model.map;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
public class Door {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@Column(name = "is_locked", nullable = false, columnDefinition = "boolean default false")
	private boolean isLocked = false;

	public void unlock() {
		this.isLocked = false;
	}

	public void lock() {
		this.isLocked = true;
	}

	@Builder
	public Door(long id, boolean isLocked) {
		this.id = id;
		this.isLocked = isLocked;
	}

	public static Door setup(WayOut wo1, WayOut wo2) {
		Door door = new Door();
		wo1.installDoor(door);
		wo2.installDoor(door);
		return door;
	}

	public boolean isLocked() {
		return isLocked;
	}
}
