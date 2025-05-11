package com.jefflife.mudmk2.gamedata.application.domain.model.player;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter @NoArgsConstructor(access = AccessLevel.PROTECTED)
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
    private boolean alive = true;

    @Builder
    public BaseCharacter(
            final String name,
            final String background,
            final int hp,
            final int maxHp,
            final int mp,
            final int maxMp,
            final int str,
            final int dex,
            final int con,
            final int intelligence,
            final int pow,
            final int cha,
            final Long roomId,
            final boolean alive
    ) {
        this.name = name;
        this.background = background;
        this.hp = hp;
        this.maxHp = maxHp;
        this.mp = mp;
        this.maxMp = maxMp;
        this.str = str;
        this.dex = dex;
        this.con = con;
        this.intelligence = intelligence;
        this.pow = pow;
        this.cha = cha;
        this.roomId = roomId;
        this.alive = alive;
    }
}