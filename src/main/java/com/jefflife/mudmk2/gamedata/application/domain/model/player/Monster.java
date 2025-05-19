package com.jefflife.mudmk2.gamedata.application.domain.model.player;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder @AllArgsConstructor
@Getter @NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Monster {
    private String id; // UUID 문자열로 관리
    private String name;
    private String description;
    private int level;
    private Long monsterTypeId; // 원본 MonsterType의 ID

    private int hp;
    private int maxHp;
    private int mp;
    private int maxMp;
    private int strength;
    private int dexterity;
    private int constitution;
    private int intelligence;
    private int power;
    private int charisma;

    private long experienceReward;
    private LocalDateTime lastDeathTime;
    private Long currentRoomId;
    private int aggressiveness; // 0-100 범위의 공격성
    private int respawnTime; // 초 단위 리스폰 시간

    private boolean isDead;

    /**
     * MonsterType을 기반으로 Monster 인스턴스를 생성합니다.
     * @param monsterType 몬스터 타입
     * @param level 몬스터 레벨
     * @param roomId 스폰할 방 ID
     * @return 생성된 Monster 인스턴스
     */
    public static Monster createFromType(MonsterType monsterType, int level, Long roomId) {
        // 레벨 기반으로 스탯 계산
        int maxHp = monsterType.getBaseHp() + (level * monsterType.getHpPerLevel());
        int maxMp = monsterType.getBaseMp();
        int strength = monsterType.getBaseStr() + (level * monsterType.getStrPerLevel());
        int dexterity = monsterType.getBaseDex() + (level * monsterType.getDexPerLevel());
        int constitution = monsterType.getBaseCon() + (level * monsterType.getConPerLevel());
        int intelligence = monsterType.getBaseIntelligence() + (level * monsterType.getIntelligencePerLevel());
        int power = monsterType.getBasePow() + (level * monsterType.getPowPerLevel());
        int charisma = monsterType.getBaseCha() + (level * monsterType.getChaPerLevel());
        long experienceReward = monsterType.getBaseExperience() + (level * monsterType.getExpPerLevel());

        return Monster.builder()
                .id(UUID.randomUUID().toString())
                .name(monsterType.getName())
                .description(monsterType.getDescription())
                .level(level)
                .monsterTypeId(monsterType.getId())
                .hp(maxHp)
                .maxHp(maxHp)
                .mp(maxMp)
                .maxMp(maxMp)
                .strength(strength)
                .dexterity(dexterity)
                .constitution(constitution)
                .intelligence(intelligence)
                .power(power)
                .charisma(charisma)
                .experienceReward(experienceReward)
                .currentRoomId(roomId)
                .aggressiveness(monsterType.getAggressiveness())
                .respawnTime(monsterType.getRespawnTime())
                .isDead(false)
                .build();
    }

    /**
     * 몬스터가 죽은 경우 호출됩니다.
     */
    public void die() {
        this.hp = 0;
        this.isDead = true;
        this.lastDeathTime = LocalDateTime.now();
    }

    /**
     * 몬스터를 리스폰합니다.
     */
    public void respawn() {
        this.hp = this.maxHp;
        this.mp = this.maxMp;
        this.isDead = false;
    }

    /**
     * 남은 리스폰 시간(초)을 반환합니다.
     * @return 남은 리스폰 시간(초), 죽지 않은 경우 0 반환
     */
    public int getRemainingRespawnTime() {
        if (!isDead || lastDeathTime == null) {
            return 0;
        }

        long elapsedSeconds = java.time.Duration.between(lastDeathTime, LocalDateTime.now()).getSeconds();
        int remaining = respawnTime - (int) elapsedSeconds;
        return Math.max(0, remaining);
    }

    /**
     * 리스폰 가능한지 확인합니다.
     * @return 리스폰 가능 여부
     */
    public boolean canRespawn() {
        return isDead && getRemainingRespawnTime() <= 0;
    }

    /**
     * 몬스터가 대미지를 입습니다.
     * @param damage 입을 대미지 양
     * @return 몬스터가 죽었는지 여부
     */
    public boolean takeDamage(int damage) {
        this.hp = Math.max(0, this.hp - damage);
        if (this.hp == 0) {
            die();
            return true;
        }
        return false;
    }
}
