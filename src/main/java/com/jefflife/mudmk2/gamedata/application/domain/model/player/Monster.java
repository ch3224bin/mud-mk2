package com.jefflife.mudmk2.gamedata.application.domain.model.player;

import jakarta.persistence.Embedded;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@EqualsAndHashCode(of = "id")
@Builder @AllArgsConstructor
@Getter @NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Monster implements Combatable, Statable {
    private UUID id;
    private String description;
    private int level;
    private Long monsterTypeId; // 원본 MonsterType의 ID

    @Embedded
    private BaseCharacter baseCharacterInfo;

    private long experienceReward;
    private LocalDateTime lastDeathTime;
    private int aggressiveness; // 0-100 범위의 공격성
    private int respawnTime; // 초 단위 리스폰 시간

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

        BaseCharacter baseCharacterInfo = BaseCharacter.builder()
                .hp(maxHp)
                .maxHp(maxHp)
                .mp(maxMp)
                .maxMp(maxMp)
                .str(strength)
                .dex(dexterity)
                .con(constitution)
                .intelligence(intelligence)
                .pow(power)
                .cha(charisma)
                .name(monsterType.getName())
                .background(monsterType.getDescription())
                .roomId(roomId)
                .alive(true)
                .gender(monsterType.getGender())
                .build();

        return Monster.builder()
                .id(UUID.randomUUID())
                .description(monsterType.getDescription())
                .level(level)
                .monsterTypeId(monsterType.getId())
                .experienceReward(experienceReward)
                .aggressiveness(monsterType.getAggressiveness())
                .respawnTime(monsterType.getRespawnTime())
                .baseCharacterInfo(baseCharacterInfo)
                .build();
    }

    /**
     * 몬스터를 리스폰합니다.
     */
    public void respawn() {
        this.baseCharacterInfo.fullRestore();
    }

    /**
     * 남은 리스폰 시간(초)을 반환합니다.
     * @return 남은 리스폰 시간(초), 죽지 않은 경우 0 반환
     */
    public int getRemainingRespawnTime() {
        if (!this.baseCharacterInfo.isAlive() || lastDeathTime == null) {
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
        return !this.baseCharacterInfo.isAlive() && getRemainingRespawnTime() <= 0;
    }

    public Long getCurrentRoomId() {
        return this.baseCharacterInfo.getRoomId();
    }

    public String getName() {
        return baseCharacterInfo.getName();
    }

    @Override
    public CharacterStats getStats() {
        return this.baseCharacterInfo.getStats();
    }

    @Override
    public void enterCombatState() {
        this.baseCharacterInfo.setState(CharacterState.COMBAT);
    }

    @Override
    public void damaged(int damage) {
        baseCharacterInfo.decreaseHp(damage);
    }

    public boolean isAlive() {
        return this.baseCharacterInfo.isAlive();
    }

    @Override
    public CharacterState getState() {
        return this.baseCharacterInfo.getState();
    }
}
