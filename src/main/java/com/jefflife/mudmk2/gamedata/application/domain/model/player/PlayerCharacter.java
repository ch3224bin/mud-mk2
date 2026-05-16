package com.jefflife.mudmk2.gamedata.application.domain.model.player;

import com.jefflife.mudmk2.gamedata.application.domain.model.item.StatType;
import com.jefflife.mudmk2.gamedata.application.domain.model.map.Direction;
import com.jefflife.mudmk2.gamedata.application.domain.model.map.Room;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@EqualsAndHashCode(of = "id")
@Getter @NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class PlayerCharacter implements Combatable, Statable {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Embedded
    private BaseCharacter baseCharacterInfo;

    @Embedded
    private PlayableCharacter playableCharacterInfo;

    private Long userId;
    private String nickname;

    @Enumerated(EnumType.STRING)
    private CharacterClass characterClass;

    private boolean online = false;
    private LocalDateTime lastActiveAt;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "inventory_id")
    private Inventory inventory;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "equipped_items_id")
    private EquippedItems equippedItems;

    public PlayerCharacter(
            final UUID id,
            final BaseCharacter baseCharacterInfo,
            final PlayableCharacter playableCharacterInfo,
            final Long userId,
            final String nickname,
            final CharacterClass characterClass,
            final boolean online,
            final LocalDateTime lastActiveAt,
            final Inventory inventory,
            final EquippedItems equippedItems
    ) {
        this.id = id;
        this.baseCharacterInfo = baseCharacterInfo;
        this.playableCharacterInfo = playableCharacterInfo;
        this.userId = userId;
        this.nickname = nickname;
        this.characterClass = characterClass;
        this.online = online;
        this.lastActiveAt = lastActiveAt;
        this.inventory = inventory;
        this.equippedItems = equippedItems;
    }

    public void initializeAssociatedEntities() {
        this.inventory.initializeAssociatedEntities();
        this.equippedItems.initializeAssociatedEntities();
    }

    public MoveResult move(final Room currentRoom, final Direction direction) {
        if (!currentRoom.hasWay(direction)) {
            return MoveResult.NO_WAY;
        }
        if (currentRoom.isLocked(direction)) {
            return MoveResult.LOCKED;
        }
        Optional<Room> nextRoomByDirection = currentRoom.getNextRoomByDirection(direction);
        if (nextRoomByDirection.isPresent()) {
            Room nextRoom = nextRoomByDirection.get();
            this.setCurrentRoomId(nextRoom.getId());
            return MoveResult.SUCCESS;
        } else {
            return MoveResult.FAILED;
        }
    }

    public void setCurrentRoomId(final Long roomId) {
        this.baseCharacterInfo.setRoomId(roomId);
    }

    public Long getCurrentRoomId() {
        return this.baseCharacterInfo.getRoomId();
    }

    @Override
    public String getName() {
        return this.nickname;
    }

    public CharacterStats getBaseStats() {
        return this.baseCharacterInfo.getStats();
    }

    public Map<StatType, Integer> getStatModifiers() {
        return equippedItems.sumStatModifiers();
    }

    @Override
    public CharacterStats getStats() {
        CharacterStats base = baseCharacterInfo.getStats();
        Map<StatType, Integer> mods = equippedItems.sumStatModifiers();
        return new CharacterStats(
                base.hp(), base.mp(), base.ap(),
                base.vigor()            + mods.getOrDefault(StatType.VIGOR, 0),
                base.physique()         + mods.getOrDefault(StatType.PHYSIQUE, 0),
                base.agility()          + mods.getOrDefault(StatType.AGILITY, 0),
                base.intellect()        + mods.getOrDefault(StatType.INTELLECT, 0),
                base.will()             + mods.getOrDefault(StatType.WILL, 0),
                base.meridian()         + mods.getOrDefault(StatType.MERIDIAN, 0),
                base.innerPower()       + mods.getOrDefault(StatType.INNER_POWER, 0),
                base.specialTechnique() + mods.getOrDefault(StatType.SPECIAL_TECHNIQUE, 0),
                base.lightStep()        + mods.getOrDefault(StatType.LIGHT_STEP, 0),
                base.fistsAndPalms()    + mods.getOrDefault(StatType.FISTS_AND_PALMS, 0),
                base.swordMethod()      + mods.getOrDefault(StatType.SWORD_METHOD, 0),
                base.bladeMethod()      + mods.getOrDefault(StatType.BLADE_METHOD, 0),
                base.longWeapon()       + mods.getOrDefault(StatType.LONG_WEAPON, 0),
                base.esotericWeapon()   + mods.getOrDefault(StatType.ESOTERIC_WEAPON, 0),
                base.archery()          + mods.getOrDefault(StatType.ARCHERY, 0)
        );
    }

    @Override
    public CharacterState getState() {
        return this.baseCharacterInfo.getState();
    }

    @Override
    public void enterCombatState() {
        this.baseCharacterInfo.setState(CharacterState.COMBAT);
    }

    @Override
    public void damaged(int damage) {
        baseCharacterInfo.decreaseHp(damage);
    }

    @Override
    public boolean isAlive() {
        return baseCharacterInfo.isAlive();
    }

    public void fullRestore() {
        baseCharacterInfo.fullRestore();
    }

    public enum MoveResult {
        NO_WAY,
        LOCKED,
        SUCCESS,
        FAILED
    }
}
