package com.jefflife.mudmk2.gameplay.application.domain.model.combat;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
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

    @DisplayName("전투 액션 테스트 - 고정된 주사위 값으로 테스트")
    @Test
    void testCombatAction() {
        // given
        // 1. 고정된 주사위 값을 반환하는 DiceRoller 생성
        // 공격 주사위, 방어 주사위, 데미지 주사위 순서로 값 설정
        FixedDiceRoller fixedDiceRoller = new FixedDiceRoller(
                15, // 첫 번째 공격자의 공격 주사위
                10, // 첫 번째 방어자의 방어 주사위
                5,  // 첫 번째 공격자의 데미지 주사위
                8,  // 두 번째 공격자의 공격 주사위
                12, // 두 번째 방어자의 방어 주사위
                0,  // 두 번째 공격자의 공격은 빗나감 (데미지 주사위 사용 안 함)
                18, // 첫 번째 적의 공격 주사위
                5,  // 첫 번째 아군의 방어 주사위
                6,  // 첫 번째 적의 데미지 주사위
                7   // 두 번째 적의 공격 주사위 (이후 값은 필요에 따라 추가)
        );

        // 2. 아군 그룹 설정 (PlayerCharacter와 NonPlayerCharacter)
        CombatGroup allyGroup = new CombatGroup(CombatGroupType.ALLY);
        allyGroup.addParticipant(new CombatParticipant(createPlayerCharacter(14))); // 민첩 14의 플레이어 캐릭터
        allyGroup.addParticipant(new CombatParticipant(createNpc(12))); // 민첩 12의 NPC

        // 3. 적 그룹 설정 (Monster 두 개)
        CombatGroup enemyGroup = new CombatGroup(CombatGroupType.ENEMY);
        enemyGroup.addParticipant(new CombatParticipant(createMonster(10))); // 민첩 10의 몬스터
        enemyGroup.addParticipant(new CombatParticipant(createMonster(8))); // 민첩 8의 몬스터

        // 4. 선제권 제공자 설정 (고정된 값으로)
        InitiativeProvider initiativeProvider = stats -> {
            int diceRoll = 10; // 고정된 주사위 값으로 테스트
            int dexBonus = stats.getDexterityModifier();
            return new InitiativeRoll(diceRoll, dexBonus, 0, 0);
        };

        // 5. Combat 객체 생성
        Combat combat = new Combat(UUID.randomUUID(), allyGroup, enemyGroup, initiativeProvider, fixedDiceRoller);

        // when
        // 1. 전투 시작
        CombatStartResult startResult = combat.start();

        // 2. 액션 실행
        CombatActionResult actionResult = combat.action();

        // then
        // 1. 선제권 확인
        assertThat(startResult.initiativeGroup()).isEqualTo(CombatGroupType.ALLY);

        // 2. 액션 결과 확인
        assertThat(actionResult.isActed()).isTrue();

        // 3. 로그 확인
        List<CombatLog> logs = actionResult.getLogs();
        assertThat(logs).isNotEmpty();

        // 디버그 로깅
        System.out.println("[DEBUG_LOG] Number of logs: " + logs.size());
        for (int i = 0; i < logs.size(); i++) {
            CombatLog log = logs.get(i);
            System.out.println("[DEBUG_LOG] Log " + i + ": " +
                    "attacker=" + log.attackerName() + ", " +
                    "target=" + log.targetName() + ", " +
                    "attackRoll=" + log.attackRoll() + ", " +
                    "defenseRoll=" + log.defenseRoll() + ", " +
                    "hitSuccess=" + log.hitSuccess());
        }

        // 4. 첫 번째 로그 확인 (아군 플레이어의 공격)
        CombatLog firstLog = logs.get(0);
        assertThat(firstLog.hitSuccess()).isTrue(); // 15 (공격) > 10 (방어)
        assertThat(firstLog.attackRoll()).isEqualTo(15);
        assertThat(firstLog.defenseRoll()).isEqualTo(10);
        assertThat(firstLog.baseDamage()).isEqualTo(5); // 데미지 주사위 값

        // 5. 두 번째 로그 확인 (아군 NPC의 공격)
        CombatLog secondLog = logs.get(1);
        assertThat(secondLog.hitSuccess()).isFalse(); // 8 (공격) < 12 (방어)
        assertThat(secondLog.attackRoll()).isEqualTo(8);
        assertThat(secondLog.defenseRoll()).isEqualTo(12);

        // 6. 세 번째 로그 확인 (적 몬스터의 공격)
        CombatLog thirdLog = logs.get(2);
        assertThat(thirdLog.hitSuccess()).isFalse(); // 0 (공격) < 18 (방어)
        assertThat(thirdLog.attackRoll()).isEqualTo(0);
        assertThat(thirdLog.defenseRoll()).isEqualTo(18);

        // 7. 네 번째 로그 확인 (두 번째 적 몬스터의 공격)
        CombatLog fourthLog = logs.get(3);
        assertThat(fourthLog.hitSuccess()).isFalse(); // 5 (공격) < 6 (방어)
        assertThat(fourthLog.attackRoll()).isEqualTo(5);
        assertThat(fourthLog.defenseRoll()).isEqualTo(6);
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
