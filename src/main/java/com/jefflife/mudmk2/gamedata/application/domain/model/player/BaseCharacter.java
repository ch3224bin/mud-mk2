package com.jefflife.mudmk2.gamedata.application.domain.model.player;

import jakarta.persistence.*;
import lombok.*;

@Embeddable
@Getter @NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder @AllArgsConstructor
public class BaseCharacter {
    private String name;
    
    @Column(length = 1000)
    private String background;
    
    // 스탯
    private int hp;
    private int maxHp;
    private int mp;
    private int maxMp;
    private int str; // 힘
    private int dex; // 민첩
    private int con; // 체력
    private int intelligence; // 지능
    private int pow; // 마력
    private int cha; // 매력
    
    // 위치 정보
    private Long roomId;
    
    // 생존 여부
    @Builder.Default
    private boolean alive = true;

    public void setRoomId(final Long roomId) {
        this.roomId = roomId;
    }
}