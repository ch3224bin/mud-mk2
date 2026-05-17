package com.jefflife.mudmk2.gameplay.application.service.command.martialart;

import com.jefflife.mudmk2.gamedata.application.domain.model.item.StatModifier;
import com.jefflife.mudmk2.gamedata.application.domain.model.item.WeaponType;
import com.jefflife.mudmk2.gamedata.application.domain.model.martialart.*;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.PlayerCharacter;
import com.jefflife.mudmk2.gamedata.application.service.provided.ExternalArtTemplateFinder;
import com.jefflife.mudmk2.gamedata.application.service.provided.LearnedMartialArtFinder;
import com.jefflife.mudmk2.gamedata.application.service.provided.MentalMethodTemplateFinder;
import com.jefflife.mudmk2.gameplay.application.service.command.look.ItemDisplayLabels;
import com.jefflife.mudmk2.gameplay.application.service.model.template.MartialArtViewVariables;
import com.jefflife.mudmk2.gameplay.application.service.model.template.StatusVariables;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class MartialArtViewMapper {

    private final LearnedMartialArtFinder learnedFinder;
    private final MentalMethodTemplateFinder mentalTplFinder;
    private final ExternalArtTemplateFinder externalTplFinder;

    public MartialArtViewMapper(LearnedMartialArtFinder learnedFinder,
                                MentalMethodTemplateFinder mentalTplFinder,
                                ExternalArtTemplateFinder externalTplFinder) {
        this.learnedFinder = learnedFinder;
        this.mentalTplFinder = mentalTplFinder;
        this.externalTplFinder = externalTplFinder;
    }

    public StatusVariables.EquippedMartialArtsView toEquippedView(PlayerCharacter pc) {
        LearnedMartialArtFinder.CharacterMartialArtView v = learnedFinder.findByCharacter(pc.getId());

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
        if (learned == null) {
            throw new IllegalStateException(
                    "Equipped mental slot references unknown learnedId " + learnedId);
        }
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
        if (learned == null) {
            throw new IllegalStateException(
                    "Equipped external slot references unknown learnedId " + learnedId);
        }
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

    public MartialArtViewVariables toMartialArtVariables(Long userId, PlayerCharacter pc) {
        LearnedMartialArtFinder.CharacterMartialArtView v = learnedFinder.findByCharacter(pc.getId());
        Set<UUID> equippedMentalIds = new HashSet<>(v.equipped().getMentalSlots().values());
        Set<UUID> equippedExternalIds = new HashSet<>(v.equipped().getExternalSlots());

        return new MartialArtViewVariables(
                userId,
                groupMental(v.learnedMentalMethods(), equippedMentalIds),
                groupExternal(v.learnedExternalArts(), equippedExternalIds));
    }

    private List<MartialArtViewVariables.MentalGroup> groupMental(
            List<LearnedMentalMethod> learned, Set<UUID> equippedIds) {
        EnumMap<MentalMethodKind, List<MartialArtViewVariables.LearnedMentalLine>> buckets =
                new EnumMap<>(MentalMethodKind.class);
        for (LearnedMentalMethod m : learned) {
            MentalMethodTemplate tpl = mentalTplFinder.findById(m.getMentalMethodTemplateId());
            List<MartialArtViewVariables.StatModLine> effects = tpl.effectAt(m.getCurrentLevel())
                    .statModifiers().stream()
                    .map(this::toViewStatModLine)
                    .toList();
            MartialArtViewVariables.LearnedMentalLine line = new MartialArtViewVariables.LearnedMentalLine(
                    tpl.getName(),
                    m.getCurrentLevel(),
                    tpl.getMaxLevel(),
                    m.getCurrentExp(),
                    m.getCurrentLevel() == tpl.getMaxLevel(),
                    equippedIds.contains(m.getId()),
                    effects);
            buckets.computeIfAbsent(tpl.getKind(), k -> new ArrayList<>()).add(line);
        }
        List<MartialArtViewVariables.MentalGroup> groups = new ArrayList<>();
        for (MentalMethodKind kind : MentalMethodKind.values()) {
            List<MartialArtViewVariables.LearnedMentalLine> items = buckets.get(kind);
            if (items != null && !items.isEmpty()) {
                groups.add(new MartialArtViewVariables.MentalGroup(ItemDisplayLabels.of(kind), items));
            }
        }
        return groups;
    }

    private List<MartialArtViewVariables.ExternalGroup> groupExternal(
            List<LearnedExternalArt> learned, Set<UUID> equippedIds) {
        EnumMap<WeaponType,
                List<MartialArtViewVariables.LearnedExternalLine>> buckets =
                new EnumMap<>(WeaponType.class);
        for (LearnedExternalArt e : learned) {
            ExternalArtTemplate tpl = externalTplFinder.findById(e.getExternalArtTemplateId());
            ExternalArtLevelEffect ef = tpl.effectAt(e.getCurrentLevel());
            MartialArtViewVariables.LearnedExternalLine line = new MartialArtViewVariables.LearnedExternalLine(
                    tpl.getName(),
                    e.getCurrentLevel(),
                    tpl.getMaxLevel(),
                    e.getCurrentExp(),
                    e.getCurrentLevel() == tpl.getMaxLevel(),
                    equippedIds.contains(e.getId()),
                    ef.damageMultiplier(),
                    ef.cooldownSeconds(),
                    ef.apCost(),
                    ef.mpCost());
            buckets.computeIfAbsent(tpl.getWeaponType(), k -> new ArrayList<>()).add(line);
        }
        List<MartialArtViewVariables.ExternalGroup> groups = new ArrayList<>();
        for (WeaponType wt :
                WeaponType.values()) {
            List<MartialArtViewVariables.LearnedExternalLine> items = buckets.get(wt);
            if (items != null && !items.isEmpty()) {
                groups.add(new MartialArtViewVariables.ExternalGroup(ItemDisplayLabels.of(wt), items));
            }
        }
        return groups;
    }

    private MartialArtViewVariables.StatModLine toViewStatModLine(StatModifier mod) {
        return new MartialArtViewVariables.StatModLine(
                ItemDisplayLabels.of(mod.getStatType()), mod.getValue());
    }
}
