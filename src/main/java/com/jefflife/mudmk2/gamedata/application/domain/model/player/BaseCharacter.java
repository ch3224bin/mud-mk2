package com.jefflife.mudmk2.gamedata.application.domain.model.player;

import jakarta.persistence.*;
import lombok.*;

@Embeddable
@Getter @NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder @AllArgsConstructor
public class BaseCharacter {
    private String name;

    @Builder.Default
    private CharacterState state = CharacterState.NORMAL;

    @Column(length = 1000)
    private String background;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Gender gender = Gender.MALE;

    // 현재 자원
    private int hp;
    private int mp;
    private int ap;

    // 속성 (屬性) — 6개
    private int vigor;            // 근력(筋力)
    private int physique;         // 체질(體質)
    private int agility;          // 민첩(敏捷)
    private int intellect;        // 지력(智力)
    private int will;             // 의지(意志)
    private int meridian;         // 경맥(經脈)

    // 무예 (武藝) — 9개
    private int innerPower;       // 내공(內功)
    private int specialTechnique; // 절기(絶技)
    private int lightStep;        // 경공(輕功)
    private int fistsAndPalms;    // 권장(拳掌)
    private int swordMethod;      // 검법(劍法)
    private int bladeMethod;      // 도법(刀法)
    private int longWeapon;       // 장병(長兵)
    private int esotericWeapon;   // 기문(奇門)
    private int archery;          // 사술(射術)

    // 위치 정보
    private Long roomId;

    @Builder.Default
    private boolean alive = true;

    public void setRoomId(final Long roomId) {
        this.roomId = roomId;
    }

    public void moveTo(final Long roomId) {
        if (roomId == null) {
            throw new IllegalArgumentException("roomId must not be null");
        }
        this.roomId = roomId;
    }

    public CharacterStats getStats() {
        return new CharacterStats(
                hp, mp, ap,
                vigor, physique, agility, intellect, will, meridian,
                innerPower, specialTechnique, lightStep,
                fistsAndPalms, swordMethod, bladeMethod, longWeapon, esotericWeapon, archery
        );
    }

    public void setState(CharacterState characterState) {
        this.state = characterState;
    }

    public void fullRestore() {
        CharacterStats stats = getStats();
        this.hp = stats.maxHp();
        this.mp = stats.maxMp();
        this.ap = stats.maxAp();
        this.alive = true;
    }

    public void decreaseHp(int amount) {
        this.hp -= amount;
        if (this.hp <= 0) {
            this.alive = false;
            this.state = CharacterState.DEAD;
        }
    }

    public void healHp(int amount, int maxHp) {
        this.hp = Math.min(this.hp + amount, maxHp);
    }

    public void healMp(int amount, int maxMp) {
        this.mp = Math.min(this.mp + amount, maxMp);
    }

    public void healAp(int amount, int maxAp) {
        this.ap = Math.min(this.ap + amount, maxAp);
    }
}
