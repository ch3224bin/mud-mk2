package com.jefflife.mudmk2.gameplay.application.service.command.martialart;

import com.jefflife.mudmk2.gamedata.application.domain.model.item.StatModifier;
import com.jefflife.mudmk2.gamedata.application.domain.model.item.StatType;
import com.jefflife.mudmk2.gamedata.application.domain.model.item.WeaponType;
import com.jefflife.mudmk2.gamedata.application.domain.model.martialart.*;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.PlayerCharacter;
import com.jefflife.mudmk2.gamedata.application.service.provided.ExternalArtTemplateFinder;
import com.jefflife.mudmk2.gamedata.application.service.provided.LearnedMartialArtFinder;
import com.jefflife.mudmk2.gamedata.application.service.provided.MentalMethodTemplateFinder;
import com.jefflife.mudmk2.gameplay.application.service.model.template.MartialArtViewVariables;
import com.jefflife.mudmk2.gameplay.application.service.model.template.StatusVariables;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MartialArtViewMapperTest {

    private LearnedMartialArtFinder learnedFinder;
    private MentalMethodTemplateFinder mentalTplFinder;
    private ExternalArtTemplateFinder externalTplFinder;
    private MartialArtViewMapper mapper;

    private PlayerCharacter pc;
    private UUID pcId;

    @BeforeEach
    void setUp() {
        learnedFinder = mock(LearnedMartialArtFinder.class);
        mentalTplFinder = mock(MentalMethodTemplateFinder.class);
        externalTplFinder = mock(ExternalArtTemplateFinder.class);
        mapper = new MartialArtViewMapper(learnedFinder, mentalTplFinder, externalTplFinder);

        pcId = UUID.randomUUID();
        pc = mock(PlayerCharacter.class);
        when(pc.getId()).thenReturn(pcId);
    }

    private LearnedMartialArtFinder.CharacterMartialArtView view(
            List<LearnedMentalMethod> mentals,
            List<LearnedExternalArt> externals,
            EquippedMartialArts equipped) {
        return new LearnedMartialArtFinder.CharacterMartialArtView(pcId, mentals, externals, equipped);
    }

    // ---- toEquippedView ----

    @Test
    void toEquippedView_noEquipped_threeMentalSlotsAndSixExternalSlotsAllEmpty() {
        when(learnedFinder.findByCharacter(pcId))
                .thenReturn(view(List.of(), List.of(), EquippedMartialArts.create()));

        StatusVariables.EquippedMartialArtsView v = mapper.toEquippedView(pc);

        assertThat(v.mentalSlots()).hasSize(3);
        assertThat(v.mentalSlots()).allMatch(s -> s.name() == null);
        assertThat(v.mentalSlots().get(0).kindLabel()).isEqualTo("내공");
        assertThat(v.mentalSlots().get(1).kindLabel()).isEqualTo("경공");
        assertThat(v.mentalSlots().get(2).kindLabel()).isEqualTo("특기");

        assertThat(v.externalSlots()).hasSize(6);
        assertThat(v.externalSlots()).allMatch(s -> s.name() == null);
        assertThat(v.externalSlots().get(0).slotNumber()).isEqualTo(1);
        assertThat(v.externalSlots().get(5).slotNumber()).isEqualTo(6);
    }

    @Test
    void toEquippedView_withEquippedMentalAndExternal_populatesSlots() {
        // 심법: INNER_POWER 슬롯에 천뢰신공 Lv.2 장착
        MentalMethodTemplate mentalTpl = mock(MentalMethodTemplate.class);
        when(mentalTpl.getName()).thenReturn("천뢰신공");
        when(mentalTpl.getMaxLevel()).thenReturn(3);
        when(mentalTpl.effectAt(2)).thenReturn(new MentalMethodLevelEffect(2,
                List.of(new StatModifier(StatType.INNER_POWER, 3))));

        UUID learnedMentalId = UUID.randomUUID();
        LearnedMentalMethod learnedMental = mock(LearnedMentalMethod.class);
        when(learnedMental.getId()).thenReturn(learnedMentalId);
        when(learnedMental.getMentalMethodTemplateId()).thenReturn(100L);
        when(learnedMental.getCurrentLevel()).thenReturn(2);

        when(mentalTplFinder.findById(100L)).thenReturn(mentalTpl);

        // 외공: 슬롯 1번에 천뢰검법 Lv.1
        ExternalArtTemplate externalTpl = mock(ExternalArtTemplate.class);
        when(externalTpl.getName()).thenReturn("천뢰검법");
        when(externalTpl.getWeaponType()).thenReturn(WeaponType.SWORD);
        when(externalTpl.getMaxLevel()).thenReturn(5);
        when(externalTpl.effectAt(1)).thenReturn(new ExternalArtLevelEffect(1, 1.5, 4, 3, 2));

        UUID learnedExternalId = UUID.randomUUID();
        LearnedExternalArt learnedExternal = mock(LearnedExternalArt.class);
        when(learnedExternal.getId()).thenReturn(learnedExternalId);
        when(learnedExternal.getExternalArtTemplateId()).thenReturn(200L);
        when(learnedExternal.getCurrentLevel()).thenReturn(1);

        when(externalTplFinder.findById(200L)).thenReturn(externalTpl);

        EquippedMartialArts equipped = EquippedMartialArts.create();
        equipped.equipMental(MentalMethodKind.INNER_POWER, learnedMentalId);
        equipped.equipExternal(learnedExternalId);

        when(learnedFinder.findByCharacter(pcId)).thenReturn(view(
                List.of(learnedMental), List.of(learnedExternal), equipped));

        StatusVariables.EquippedMartialArtsView v = mapper.toEquippedView(pc);

        // 심법 슬롯 0 (INNER_POWER): 채워짐
        StatusVariables.MentalSlotLine inner = v.mentalSlots().get(0);
        assertThat(inner.name()).isEqualTo("천뢰신공");
        assertThat(inner.currentLevel()).isEqualTo(2);
        assertThat(inner.maxLevel()).isEqualTo(3);
        assertThat(inner.effects()).hasSize(1);
        assertThat(inner.effects().get(0).label()).isEqualTo("내공");
        assertThat(inner.effects().get(0).value()).isEqualTo(3);

        // 심법 슬롯 1, 2: 빈 채로
        assertThat(v.mentalSlots().get(1).name()).isNull();
        assertThat(v.mentalSlots().get(2).name()).isNull();

        // 외공 슬롯 0 (slotNumber 1): 채워짐
        StatusVariables.ExternalSlotLine ext = v.externalSlots().get(0);
        assertThat(ext.slotNumber()).isEqualTo(1);
        assertThat(ext.name()).isEqualTo("천뢰검법");
        assertThat(ext.weaponLabel()).isEqualTo("검");
        assertThat(ext.currentLevel()).isEqualTo(1);
        assertThat(ext.maxLevel()).isEqualTo(5);
        assertThat(ext.damageMultiplier()).isEqualTo(1.5);
        assertThat(ext.cooldownSeconds()).isEqualTo(4);
        assertThat(ext.apCost()).isEqualTo(3);
        assertThat(ext.mpCost()).isEqualTo(2);

        // 외공 슬롯 1~5: 빈 채로
        for (int i = 1; i < 6; i++) {
            assertThat(v.externalSlots().get(i).name()).isNull();
            assertThat(v.externalSlots().get(i).slotNumber()).isEqualTo(i + 1);
        }
    }

    // ---- toMartialArtVariables ----

    @Test
    void toMartialArtVariables_noLearned_returnsEmptyGroups() {
        when(learnedFinder.findByCharacter(pcId))
                .thenReturn(view(List.of(), List.of(), EquippedMartialArts.create()));

        MartialArtViewVariables v = mapper.toMartialArtVariables(7L, pc);

        assertThat(v.userId()).isEqualTo(7L);
        assertThat(v.mentalGroups()).isEmpty();
        assertThat(v.externalGroups()).isEmpty();
    }

    @Test
    void toMartialArtVariables_groupsByKindAndWeaponType_equippedAndAtMaxFlagged() {
        // 심법 학습: 천뢰신공(내공, Lv 3/3 = MAX, 장착) + 풍림보(경공, Lv 1/2)
        UUID innerLearnedId = UUID.randomUUID();
        LearnedMentalMethod innerLearned = mock(LearnedMentalMethod.class);
        when(innerLearned.getId()).thenReturn(innerLearnedId);
        when(innerLearned.getMentalMethodTemplateId()).thenReturn(11L);
        when(innerLearned.getCurrentLevel()).thenReturn(3);
        when(innerLearned.getCurrentExp()).thenReturn(0L);
        MentalMethodTemplate innerTpl = mock(MentalMethodTemplate.class);
        when(innerTpl.getKind()).thenReturn(MentalMethodKind.INNER_POWER);
        when(innerTpl.getName()).thenReturn("천뢰신공");
        when(innerTpl.getMaxLevel()).thenReturn(3);
        when(innerTpl.effectAt(3)).thenReturn(new MentalMethodLevelEffect(3,
                List.of(new StatModifier(StatType.INNER_POWER, 6))));
        when(mentalTplFinder.findById(11L)).thenReturn(innerTpl);

        UUID lightLearnedId = UUID.randomUUID();
        LearnedMentalMethod lightLearned = mock(LearnedMentalMethod.class);
        when(lightLearned.getId()).thenReturn(lightLearnedId);
        when(lightLearned.getMentalMethodTemplateId()).thenReturn(12L);
        when(lightLearned.getCurrentLevel()).thenReturn(1);
        when(lightLearned.getCurrentExp()).thenReturn(0L);
        MentalMethodTemplate lightTpl = mock(MentalMethodTemplate.class);
        when(lightTpl.getKind()).thenReturn(MentalMethodKind.LIGHT_STEP);
        when(lightTpl.getName()).thenReturn("풍림보");
        when(lightTpl.getMaxLevel()).thenReturn(2);
        when(lightTpl.effectAt(1)).thenReturn(new MentalMethodLevelEffect(1,
                List.of(new StatModifier(StatType.LIGHT_STEP, 2))));
        when(mentalTplFinder.findById(12L)).thenReturn(lightTpl);

        // 외공 학습: 천뢰검법(검, Lv 1/2, 장착)
        UUID swordLearnedId = UUID.randomUUID();
        LearnedExternalArt swordLearned = mock(LearnedExternalArt.class);
        when(swordLearned.getId()).thenReturn(swordLearnedId);
        when(swordLearned.getExternalArtTemplateId()).thenReturn(21L);
        when(swordLearned.getCurrentLevel()).thenReturn(1);
        when(swordLearned.getCurrentExp()).thenReturn(0L);
        ExternalArtTemplate swordTpl = mock(ExternalArtTemplate.class);
        when(swordTpl.getName()).thenReturn("천뢰검법");
        when(swordTpl.getWeaponType()).thenReturn(WeaponType.SWORD);
        when(swordTpl.getMaxLevel()).thenReturn(2);
        when(swordTpl.effectAt(1)).thenReturn(new ExternalArtLevelEffect(1, 1.5, 4, 3, 2));
        when(externalTplFinder.findById(21L)).thenReturn(swordTpl);

        EquippedMartialArts equipped = EquippedMartialArts.create();
        equipped.equipMental(MentalMethodKind.INNER_POWER, innerLearnedId);  // 장착
        equipped.equipExternal(swordLearnedId);                              // 장착
        // lightLearnedId 는 장착 안 함

        when(learnedFinder.findByCharacter(pcId)).thenReturn(view(
                List.of(innerLearned, lightLearned),
                List.of(swordLearned),
                equipped));

        MartialArtViewVariables v = mapper.toMartialArtVariables(7L, pc);

        // 심법: 내공 + 경공 두 그룹 (특기 학습 0 → 그룹 없음), 엔움 선언순
        assertThat(v.mentalGroups()).hasSize(2);
        assertThat(v.mentalGroups().get(0).kindLabel()).isEqualTo("내공");
        assertThat(v.mentalGroups().get(0).items()).hasSize(1);
        MartialArtViewVariables.LearnedMentalLine innerLine = v.mentalGroups().get(0).items().get(0);
        assertThat(innerLine.name()).isEqualTo("천뢰신공");
        assertThat(innerLine.currentLevel()).isEqualTo(3);
        assertThat(innerLine.maxLevel()).isEqualTo(3);
        assertThat(innerLine.atMax()).isTrue();
        assertThat(innerLine.equipped()).isTrue();
        assertThat(innerLine.effects()).hasSize(1);
        assertThat(innerLine.effects().get(0).label()).isEqualTo("내공");
        assertThat(innerLine.effects().get(0).value()).isEqualTo(6);

        assertThat(v.mentalGroups().get(1).kindLabel()).isEqualTo("경공");
        MartialArtViewVariables.LearnedMentalLine lightLine = v.mentalGroups().get(1).items().get(0);
        assertThat(lightLine.atMax()).isFalse();
        assertThat(lightLine.equipped()).isFalse();

        // 외공: 검 그룹만
        assertThat(v.externalGroups()).hasSize(1);
        assertThat(v.externalGroups().get(0).weaponLabel()).isEqualTo("검");
        MartialArtViewVariables.LearnedExternalLine swordLine =
                v.externalGroups().get(0).items().get(0);
        assertThat(swordLine.name()).isEqualTo("천뢰검법");
        assertThat(swordLine.currentLevel()).isEqualTo(1);
        assertThat(swordLine.maxLevel()).isEqualTo(2);
        assertThat(swordLine.atMax()).isFalse();
        assertThat(swordLine.equipped()).isTrue();
        assertThat(swordLine.damageMultiplier()).isEqualTo(1.5);
        assertThat(swordLine.cooldownSeconds()).isEqualTo(4);
        assertThat(swordLine.apCost()).isEqualTo(3);
        assertThat(swordLine.mpCost()).isEqualTo(2);
    }
}
