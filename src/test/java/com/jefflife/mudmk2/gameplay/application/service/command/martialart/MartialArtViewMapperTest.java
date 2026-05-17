package com.jefflife.mudmk2.gameplay.application.service.command.martialart;

import com.jefflife.mudmk2.gamedata.application.domain.model.item.StatModifier;
import com.jefflife.mudmk2.gamedata.application.domain.model.item.StatType;
import com.jefflife.mudmk2.gamedata.application.domain.model.item.WeaponType;
import com.jefflife.mudmk2.gamedata.application.domain.model.martialart.*;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.PlayerCharacter;
import com.jefflife.mudmk2.gamedata.application.service.provided.ExternalArtTemplateFinder;
import com.jefflife.mudmk2.gamedata.application.service.provided.LearnedMartialArtFinder;
import com.jefflife.mudmk2.gamedata.application.service.provided.MentalMethodTemplateFinder;
import com.jefflife.mudmk2.gameplay.application.service.model.template.StatusVariables;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MartialArtViewMapperTest {

    private LearnedMartialArtFinder finder;
    private MentalMethodTemplateFinder mentalTplFinder;
    private ExternalArtTemplateFinder externalTplFinder;
    private MartialArtViewMapper mapper;

    private PlayerCharacter pc;
    private UUID pcId;

    @BeforeEach
    void setUp() {
        finder = mock(LearnedMartialArtFinder.class);
        mentalTplFinder = mock(MentalMethodTemplateFinder.class);
        externalTplFinder = mock(ExternalArtTemplateFinder.class);
        mapper = new MartialArtViewMapper(finder, mentalTplFinder, externalTplFinder);

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
        when(finder.findByCharacter(pcId))
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

        when(finder.findByCharacter(pcId)).thenReturn(view(
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
}
