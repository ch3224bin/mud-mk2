package com.jefflife.mudmk2.gamedata.application.domain.model.instance;

import jakarta.persistence.*;
import lombok.*;

@EqualsAndHashCode(of = "id")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
public class InstanceScenario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", length = 2000, nullable = false)
    private String description;

    @Column(name = "area_id", nullable = false)
    private long areaId;

    @Column(name = "entrance_room_id", nullable = false)
    private long entranceRoomId;

    @Builder
    public InstanceScenario(long id, String title, String description, long areaId, long entranceRoomId) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.areaId = areaId;
        this.entranceRoomId = entranceRoomId;
    }

    public void update(String title, String description, long areaId, long entranceRoomId) {
        this.title = title;
        this.description = description;
        this.areaId = areaId;
        this.entranceRoomId = entranceRoomId;
    }
}
