package com.jefflife.mudmk2.gameplay.application.service.command.martialart;

import com.jefflife.mudmk2.gamedata.application.domain.model.item.StatModifier;
import com.jefflife.mudmk2.gamedata.application.domain.model.martialart.*;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.PlayerCharacter;
import com.jefflife.mudmk2.gamedata.application.service.provided.ExternalArtTemplateFinder;
import com.jefflife.mudmk2.gamedata.application.service.provided.LearnedMartialArtFinder;
import com.jefflife.mudmk2.gamedata.application.service.provided.MentalMethodTemplateFinder;
import com.jefflife.mudmk2.gameplay.application.service.command.look.ItemDisplayLabels;
import com.jefflife.mudmk2.gameplay.application.service.model.template.StatusVariables;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class MartialArtViewMapper {

    private final LearnedMartialArtFinder finder;
    private final MentalMethodTemplateFinder mentalTplFinder;
    private final ExternalArtTemplateFinder externalTplFinder;

    public MartialArtViewMapper(LearnedMartialArtFinder finder,
                                MentalMethodTemplateFinder mentalTplFinder,
                                ExternalArtTemplateFinder externalTplFinder) {
        this.finder = finder;
        this.mentalTplFinder = mentalTplFinder;
        this.externalTplFinder = externalTplFinder;
    }

    public StatusVariables.EquippedMartialArtsView toEquippedView(PlayerCharacter pc) {
        LearnedMartialArtFinder.CharacterMartialArtView v = finder.findByCharacter(pc.getId());

        Map<UUID, LearnedMentalMethod> mentalById = v.learnedMentalMethods().stream()
                .collect(Collectors.toMap(LearnedMentalMethod::getId, m -> m));
        Map<UUID, LearnedExternalArt> externalById = v.learnedExternalArts().stream()
                .collect(Collectors.toMap(LearnedExternalArt::getId, m -> m));

        List<StatusVariables.MentalSlotLine> mentalSlots = new ArrayList<>();
        for (MentalMethodKind kind : MentalMethodKind.values()) {
            UUID learnedId = v.equipped().getMentalSlots().get(kind);
            mentalSlots.add(buildMentalSlot(kind, learnedId, mentalById));
        }

        List<StatusVariables.ExternalSlotLine> externalSlots = new ArrayList<>();
        List<UUID> equippedExternals = v.equipped().getExternalSlots();
        for (int i = 0; i < EquippedMartialArts.EXTERNAL_SLOT_MAX; i++) {
            UUID learnedId = i < equippedExternals.size() ? equippedExternals.get(i) : null;
            externalSlots.add(buildExternalSlot(i + 1, learnedId, externalById));
        }
        return new StatusVariables.EquippedMartialArtsView(mentalSlots, externalSlots);
    }

    private StatusVariables.MentalSlotLine buildMentalSlot(
            MentalMethodKind kind, UUID learnedId, Map<UUID, LearnedMentalMethod> mentalById) {
        String label = ItemDisplayLabels.of(kind);
        if (learnedId == null) {
            return new StatusVariables.MentalSlotLine(label, null, null, null, List.of());
        }
        LearnedMentalMethod learned = mentalById.get(learnedId);
        MentalMethodTemplate tpl = mentalTplFinder.findById(learned.getMentalMethodTemplateId());
        MentalMethodLevelEffect effect = tpl.effectAt(learned.getCurrentLevel());
        List<StatusVariables.StatModLine> effects = effect.statModifiers().stream()
                .map(this::toStatusStatModLine)
                .toList();
        return new StatusVariables.MentalSlotLine(
                label, tpl.getName(), learned.getCurrentLevel(), tpl.getMaxLevel(), effects);
    }

    private StatusVariables.ExternalSlotLine buildExternalSlot(
            int slotNumber, UUID learnedId, Map<UUID, LearnedExternalArt> externalById) {
        if (learnedId == null) {
            return new StatusVariables.ExternalSlotLine(
                    slotNumber, null, null, null, null, null, null, null, null);
        }
        LearnedExternalArt learned = externalById.get(learnedId);
        ExternalArtTemplate tpl = externalTplFinder.findById(learned.getExternalArtTemplateId());
        ExternalArtLevelEffect effect = tpl.effectAt(learned.getCurrentLevel());
        return new StatusVariables.ExternalSlotLine(
                slotNumber,
                tpl.getName(),
                ItemDisplayLabels.of(tpl.getWeaponType()),
                learned.getCurrentLevel(),
                tpl.getMaxLevel(),
                effect.damageMultiplier(),
                effect.cooldownSeconds(),
                effect.apCost(),
                effect.mpCost()
        );
    }

    private StatusVariables.StatModLine toStatusStatModLine(StatModifier mod) {
        return new StatusVariables.StatModLine(
                ItemDisplayLabels.of(mod.getStatType()), mod.getValue());
    }
}
