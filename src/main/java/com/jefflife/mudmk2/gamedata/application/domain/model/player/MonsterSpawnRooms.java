package com.jefflife.mudmk2.gamedata.application.domain.model.player;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Embeddable;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@Getter
@Embeddable
public class MonsterSpawnRooms {
    @OneToMany(mappedBy = "monsterType", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MonsterSpawnRoom> spawnRooms = new ArrayList<>();

    public void addSpawnRoom(MonsterSpawnRoom spawnRoom) {
        if (spawnRooms == null) {
            spawnRooms = new ArrayList<>();
        }
        spawnRooms.add(spawnRoom);
    }

    public void clearAndAddAll(List<MonsterSpawnRoom> newSpawnRooms) {
        if (spawnRooms == null) {
            spawnRooms = new ArrayList<>();
        } else {
            spawnRooms.clear();
        }

        if (newSpawnRooms != null) {
            spawnRooms.addAll(newSpawnRooms);
        }
    }
}
