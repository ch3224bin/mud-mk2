package com.jefflife.mudmk2.gamedata.application.domain.model.player;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Embeddable;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@Getter
@Embeddable
public class MonsterSpawnRooms {
    @OneToMany(mappedBy = "monsterType", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MonsterSpawnRoom> spawnRooms;
}
