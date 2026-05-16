package com.jefflife.mudmk2.gameplay.application.domain.model.combat;

import com.jefflife.mudmk2.gamedata.application.domain.model.item.*;
import com.jefflife.mudmk2.gamedata.application.domain.model.martialart.EquippedMartialArts;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CombatWeaponTest {

    private PlayerCharacter playerWithSword(int swordSkill) {
        BaseCharacter base = BaseCharacter.builder()
                .name("철수").background("d").gender(Gender.MALE)
                .hp(100).mp(50).ap(80)
                .vigor(10).physique(10).agility(10).intellect(10).will(10).meridian(10)
                .innerPower(0).specialTechnique(0).lightStep(0)
                .fistsAndPalms(0).swordMethod(swordSkill).bladeMethod(0)
                .longWeapon(0).esotericWeapon(0).archery(0)
                .roomId(1L).alive(true).build();
        PlayableCharacter playable = PlayableCharacter.builder()
                .level(1).experience(0).nextLevelExp(100).conversable(true).build();
        EquippedItems equipped = EquippedItems.create();
        WeaponTemplate sword = WeaponTemplate.builder()
                .name("철검").description("d").weight(5).stackable(false)
                .weaponType(WeaponType.SWORD)
                .statModifiers(List.of()).build();
        equipped.equip(EquipmentSlot.WEAPON, new ItemInstance(sword, 1));
        return new PlayerCharacter(UUID.randomUUID(), base, playable, 1L, "철수",
                CharacterClass.WARRIOR, true, LocalDateTime.now(), Inventory.create(100), equipped, EquippedMartialArts.create());
    }

    private PlayerCharacter playerBareHanded(int fistSkill) {
        BaseCharacter base = BaseCharacter.builder()
                .name("철수").background("d").gender(Gender.MALE)
                .hp(100).mp(50).ap(80)
                .vigor(10).physique(10).agility(10).intellect(10).will(10).meridian(10)
                .innerPower(0).specialTechnique(0).lightStep(0)
                .fistsAndPalms(fistSkill).swordMethod(0).bladeMethod(0)
                .longWeapon(0).esotericWeapon(0).archery(0)
                .roomId(1L).alive(true).build();
        PlayableCharacter playable = PlayableCharacter.builder()
                .level(1).experience(0).nextLevelExp(100).conversable(true).build();
        return new PlayerCharacter(UUID.randomUUID(), base, playable, 1L, "철수",
                CharacterClass.WARRIOR, true, LocalDateTime.now(), Inventory.create(100), EquippedItems.create(), EquippedMartialArts.create());
    }

    private Combatable dummyEnemy() {
        Combatable npc = org.mockito.Mockito.mock(Combatable.class);
        org.mockito.Mockito.when(npc.getId()).thenReturn(UUID.randomUUID());
        org.mockito.Mockito.when(npc.getName()).thenReturn("다람쥐");
        CharacterStats stats = new CharacterStats(
                20, 10, 10,
                10, 10, 10, 5, 5, 5,
                0, 0, 0, 0, 0, 0, 0, 0, 0);
        org.mockito.Mockito.when(npc.getStats()).thenReturn(stats);
        org.mockito.Mockito.when(npc.isAlive()).thenReturn(true);
        return npc;
    }

    private Combat makeCombat(PlayerCharacter ally, Combatable enemy, DiceRoller roller) {
        CombatParticipant a = new CombatParticipant(ally);
        CombatParticipant e = new CombatParticipant(enemy);
        CombatGroup allyGroup = new CombatGroup(CombatGroupType.ALLY);
        allyGroup.addParticipant(a);
        CombatGroup enemyGroup = new CombatGroup(CombatGroupType.ENEMY);
        enemyGroup.addParticipant(e);
        InitiativeProvider provider = stats -> new InitiativeRoll(10, 0, 0, 0);
        return new Combat(UUID.randomUUID(), allyGroup, enemyGroup, provider, roller);
    }

    @Test
    void executeGroupAction_playerWithSword_logsWeaponNameAndUsesSwordMethodForDiceMax() {
        PlayerCharacter player = playerWithSword(7);
        Combatable enemy = dummyEnemy();
        FixedDiceRoller roller = new FixedDiceRoller(20, 7);
        Combat combat = makeCombat(player, enemy, roller);
        combat.start();
        CombatActionResult result = null;
        for (int i = 0; i < 21; i++) result = combat.action();
        assertThat(result).isNotNull();
        assertThat(result.getLogs()).isNotEmpty();
        CombatLog log = result.getLogs().get(0);
        assertThat(log.weaponName()).isEqualTo("철검");
        assertThat(roller.getLastBaseDamageMax()).isEqualTo(7);
    }

    @Test
    void executeGroupAction_playerBareHanded_usesFistsAndPalmsAndWeaponName맨손() {
        PlayerCharacter player = playerBareHanded(4);
        Combatable enemy = dummyEnemy();
        FixedDiceRoller roller = new FixedDiceRoller(20, 4);
        Combat combat = makeCombat(player, enemy, roller);
        combat.start();
        CombatActionResult result = null;
        for (int i = 0; i < 21; i++) result = combat.action();
        CombatLog log = result.getLogs().get(0);
        assertThat(log.weaponName()).isEqualTo("맨손");
        assertThat(roller.getLastBaseDamageMax()).isEqualTo(4);
    }

    @Test
    void executeGroupAction_skillValueZero_diceMaxGuardedToOne() {
        PlayerCharacter player = playerBareHanded(0);
        Combatable enemy = dummyEnemy();
        FixedDiceRoller roller = new FixedDiceRoller(20, 1);
        Combat combat = makeCombat(player, enemy, roller);
        combat.start();
        for (int i = 0; i < 21; i++) combat.action();
        assertThat(roller.getLastBaseDamageMax()).isEqualTo(1);
    }

    // Test helper
    static class FixedDiceRoller implements DiceRoller {
        private final int attackOrDefenseRoll;
        private final int baseDamageRoll;
        private int lastBaseDamageMax = -1;
        private int calls = 0;

        FixedDiceRoller(int attackOrDefenseRoll, int baseDamageRoll) {
            this.attackOrDefenseRoll = attackOrDefenseRoll;
            this.baseDamageRoll = baseDamageRoll;
        }

        @Override
        public int roll(int min, int max) {
            calls++;
            if (calls == 3) {
                lastBaseDamageMax = max;
                return baseDamageRoll;
            }
            return attackOrDefenseRoll;
        }

        int getLastBaseDamageMax() { return lastBaseDamageMax; }
    }
}
