package com.jefflife.mudmk2.gamedata.application.domain.model.player;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@Getter
@Entity
public class MonsterSpawnRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long roomId;
    @ManyToOne
    @JoinColumn(name = "monster_type_id")
    private MonsterType monsterType;
    private int spawnCount;
}
