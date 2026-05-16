package com.jefflife.mudmk2.gameplay.application.domain.model.combat;

import com.jefflife.mudmk2.gamedata.application.domain.model.item.*;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ATBCombatWeaponTest {

    @Test
    void participant_playerCharacterWithSword_derivesWeaponNameAndSkill() {
        PlayerCharacter player = makePlayer(WeaponType.SWORD, "철검", 5);
        ATBCombatParticipant p = new ATBCombatParticipant(player, CombatGroupType.ALLY);

        assertThat(p.getWeaponName()).isEqualTo("철검");
        assertThat(p.getWeaponTypeName()).isEqualTo("SWORD");
        // swordMethod base 5; no equipment modifier in this scenario → skill=5
        assertThat(p.getWeaponSkill()).isEqualTo(5);
    }

    @Test
    void participant_playerCharacterWithBow_usesArchery() {
        PlayerCharacter player = makePlayer(WeaponType.ARCHERY, "나무활", 7);
        ATBCombatParticipant p = new ATBCombatParticipant(player, CombatGroupType.ALLY);

        assertThat(p.getWeaponName()).isEqualTo("나무활");
        assertThat(p.getWeaponTypeName()).isEqualTo("ARCHERY");
        assertThat(p.getWeaponSkill()).isEqualTo(7);
    }

    @Test
    void participant_playerCharacterBareHanded_usesFISTAnd맨손() {
        BaseCharacter base = baseWithSkills(0, 0, 0, 0, 0, 4); // 마지막=fistsAndPalms=4
        PlayerCharacter player = playerOf(base, EquippedItems.create());

        ATBCombatParticipant p = new ATBCombatParticipant(player, CombatGroupType.ALLY);

        assertThat(p.getWeaponName()).isEqualTo("맨손");
        assertThat(p.getWeaponTypeName()).isEqualTo("FIST");
        assertThat(p.getWeaponSkill()).isEqualTo(4);
    }

    @Test
    void participant_nonPlayerCombatable_keepsAutoSelectButReturnsEnumName() {
        // Mock Combatable with archery as highest stat
        Combatable npc = org.mockito.Mockito.mock(Combatable.class);
        org.mockito.Mockito.when(npc.getId()).thenReturn(UUID.randomUUID());
        org.mockito.Mockito.when(npc.getName()).thenReturn("궁수");
        CharacterStats stats = new CharacterStats(
                20, 10, 10,
                10, 10, 10, 5, 5, 5,
                0, 0, 0, 0, 0, 0, 0, 0, 8);  // archery=8 highest
        org.mockito.Mockito.when(npc.getStats()).thenReturn(stats);
        org.mockito.Mockito.when(npc.isAlive()).thenReturn(true);

        ATBCombatParticipant p = new ATBCombatParticipant(npc, CombatGroupType.ENEMY);

        assertThat(p.getWeaponTypeName()).isEqualTo("ARCHERY");
        assertThat(p.getWeaponName()).isEqualTo("사술");
        assertThat(p.getWeaponSkill()).isEqualTo(8);
    }

    private PlayerCharacter makePlayer(WeaponType weaponType, String weaponName, int skillValue) {
        StatType skillType = WeaponTypeMapping.weaponSkillFor(weaponType);
        int v=0, p=0, a=0, intel=0, w=0, m=0;
        int ip=0, st=0, ls=0, fp=0, sm=0, bm=0, lw=0, ew=0, ar=0;
        switch (skillType) {
            case VIGOR -> v = skillValue;
            case PHYSIQUE -> p = skillValue;
            case AGILITY -> a = skillValue;
            case INTELLECT -> intel = skillValue;
            case WILL -> w = skillValue;
            case MERIDIAN -> m = skillValue;
            case INNER_POWER -> ip = skillValue;
            case SPECIAL_TECHNIQUE -> st = skillValue;
            case LIGHT_STEP -> ls = skillValue;
            case FISTS_AND_PALMS -> fp = skillValue;
            case SWORD_METHOD -> sm = skillValue;
            case BLADE_METHOD -> bm = skillValue;
            case LONG_WEAPON -> lw = skillValue;
            case ESOTERIC_WEAPON -> ew = skillValue;
            case ARCHERY -> ar = skillValue;
        }
        BaseCharacter base = BaseCharacter.builder()
                .name("철수").background("d").gender(Gender.MALE)
                .hp(100).mp(50).ap(80)
                .vigor(v == 0 ? 10 : v).physique(p == 0 ? 10 : p).agility(a == 0 ? 10 : a)
                .intellect(intel == 0 ? 10 : intel).will(w == 0 ? 10 : w).meridian(m == 0 ? 10 : m)
                .innerPower(ip).specialTechnique(st).lightStep(ls)
                .fistsAndPalms(fp).swordMethod(sm).bladeMethod(bm)
                .longWeapon(lw).esotericWeapon(ew).archery(ar)
                .roomId(1L).alive(true).build();

        EquippedItems equipped = EquippedItems.create();
        WeaponTemplate weapon = WeaponTemplate.builder()
                .name(weaponName).description("d").weight(5).stackable(false)
                .weaponType(weaponType)
                .statModifiers(List.of()).build();
        equipped.equip(EquipmentSlot.WEAPON, new ItemInstance(weapon, 1));

        return playerOf(base, equipped);
    }

    private BaseCharacter baseWithSkills(int sm, int bm, int lw, int ew, int ar, int fp) {
        return BaseCharacter.builder()
                .name("철수").background("d").gender(Gender.MALE)
                .hp(100).mp(50).ap(80)
                .vigor(10).physique(10).agility(10).intellect(10).will(10).meridian(10)
                .innerPower(0).specialTechnique(0).lightStep(0)
                .fistsAndPalms(fp).swordMethod(sm).bladeMethod(bm)
                .longWeapon(lw).esotericWeapon(ew).archery(ar)
                .roomId(1L).alive(true).build();
    }

    private PlayerCharacter playerOf(BaseCharacter base, EquippedItems equipped) {
        PlayableCharacter playable = PlayableCharacter.builder()
                .level(1).experience(0).nextLevelExp(100).conversable(true).build();
        return new PlayerCharacter(UUID.randomUUID(), base, playable, 1L, "철수",
                CharacterClass.WARRIOR, true, LocalDateTime.now(), Inventory.create(100), equipped);
    }
}
