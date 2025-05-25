package com.jefflife.mudmk2.gameplay.application.domain.model.combat;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

class CombatTest {

    @DisplayName("AllyGroup이 EnemyGroup 보다 선제권이 높다")
    @Test
    void testAllyGroupHasHigherInitiative() {
        // given
        CombatGroup allyGroup = new CombatGroup(CombatGroupType.ALLY);
        allyGroup.addParticipant(new CombatParticipant(createPlayerCharacter(16)));
        allyGroup.addParticipant(new CombatParticipant(createNpc(14)));

        CombatGroup enemyGroup = new CombatGroup(CombatGroupType.ENEMY);
        enemyGroup.addParticipant(new CombatParticipant(createMonster(10)));
        enemyGroup.addParticipant(new CombatParticipant(createMonster(8)));

        InitiativeProvider initiativeProvider = stats -> {
            int diceRoll = 10; // 고정된 주사위 값으로 테스트
            int dexBonus = stats.getDexterityModifier();
            return new InitiativeRoll(diceRoll, dexBonus, 0, 0);
        };

        Combat combat = new Combat(UUID.randomUUID(), allyGroup, enemyGroup, initiativeProvider);

        // when
        CombatStartResult result = combat.start();

        // then
        assertThat(result.initiativeGroup()).isEqualTo(CombatGroupType.ALLY);
        assertThat(result.allyInitiative().total()).isGreaterThanOrEqualTo(result.enemyInitiative().total());
    }

    @DisplayName("EnemyGroup이 AllyGroup 보다 선제권이 높다")
    @Test
    void testEnemyGroupHasHigherInitiative() {
        // given
        CombatGroup allyGroup = new CombatGroup(CombatGroupType.ALLY);
        allyGroup.addParticipant(new CombatParticipant(createPlayerCharacter(10)));
        allyGroup.addParticipant(new CombatParticipant(createNpc(8)));

        CombatGroup enemyGroup = new CombatGroup(CombatGroupType.ENEMY);
        enemyGroup.addParticipant(new CombatParticipant(createMonster(16)));
        enemyGroup.addParticipant(new CombatParticipant(createMonster(14)));

        InitiativeProvider initiativeProvider = stats -> {
            int diceRoll = 10; // 고정된 주사위 값으로 테스트
            int dexBonus = stats.getDexterityModifier();
            return new InitiativeRoll(diceRoll, dexBonus, 0, 0);
        };

        Combat combat = new Combat(UUID.randomUUID(), allyGroup, enemyGroup, initiativeProvider);

        // when
        CombatStartResult result = combat.start();

        // then
        assertThat(result.initiativeGroup()).isEqualTo(CombatGroupType.ENEMY);
        assertThat(result.enemyInitiative().total()).isGreaterThan(result.allyInitiative().total());
    }

    private PlayerCharacter createPlayerCharacter(int dexterity) {
        BaseCharacter baseCharacter = BaseCharacter.builder()
                .name("테스터")
                .hp(100)
                .maxHp(100)
                .mp(50)
                .maxMp(50)
                .str(12)
                .dex(dexterity) // 파라미터로 받은 민첩성 사용
                .con(14)
                .intelligence(10)
                .pow(8)
                .cha(10)
                .roomId(1L)
                .build();

        PlayableCharacter playableCharacter = PlayableCharacter.builder()
                .level(5)
                .experience(500)
                .nextLevelExp(1000)
                .conversable(true)
                .build();

        return new PlayerCharacter(
                UUID.randomUUID(),
                baseCharacter,
                playableCharacter,
                1L,
                "testUser",
                CharacterClass.WARRIOR,
                true,
                LocalDateTime.now()
        );
    }

    private NonPlayerCharacter createNpc(int dexterity) {
        BaseCharacter baseCharacter = BaseCharacter.builder()
                .name("조력자NPC")
                .hp(80)
                .maxHp(80)
                .mp(40)
                .maxMp(40)
                .str(10)
                .dex(dexterity) // 파라미터로 받은 민첩성 사용
                .con(12)
                .intelligence(14)
                .pow(12)
                .cha(16)
                .roomId(1L)
                .build();

        PlayableCharacter playableCharacter = PlayableCharacter.builder()
                .level(4)
                .experience(0)
                .nextLevelExp(0)
                .conversable(true)
                .build();

        return new NonPlayerCharacter(
                UUID.randomUUID(),
                baseCharacter,
                playableCharacter,
                "조력자",
                NPCType.MERCHANT,
                Map.of("greeting", "안녕하세요!"),
                1L,
                false
        );
    }

    private Monster createMonster(int dexterity) {
        MonsterType monsterType = MonsterType.builder()
                .name("테스트몬스터")
                .description("테스트용 몬스터입니다.")
                .baseHp(50)
                .baseMp(20)
                .baseStr(10)
                .baseDex(dexterity) // 파라미터로 받은 민첩성 사용
                .baseCon(10)
                .baseIntelligence(8)
                .basePow(8)
                .baseCha(5)
                .build();

        return Monster.createFromType(monsterType, 1, 1L);
    }
}

