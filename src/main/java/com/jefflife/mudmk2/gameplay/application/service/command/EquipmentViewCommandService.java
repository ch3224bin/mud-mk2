package com.jefflife.mudmk2.gameplay.application.service.command;

import com.jefflife.mudmk2.gamedata.application.domain.model.item.EquipmentSlot;
import com.jefflife.mudmk2.gamedata.application.domain.model.item.EquippableItemTemplate;
import com.jefflife.mudmk2.gamedata.application.domain.model.item.ItemInstance;
import com.jefflife.mudmk2.gamedata.application.domain.model.item.StatModifier;
import com.jefflife.mudmk2.gamedata.application.domain.model.item.StatType;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.CharacterStats;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.EquippedItems;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.PlayerCharacter;
import com.jefflife.mudmk2.gameplay.application.domain.model.command.EquipmentViewCommand;
import com.jefflife.mudmk2.gameplay.application.exception.PlayerNotFoundException;
import com.jefflife.mudmk2.gameplay.application.service.command.look.ItemDisplayLabels;
import com.jefflife.mudmk2.gameplay.application.service.model.template.EquipmentViewVariables;
import com.jefflife.mudmk2.gameplay.application.service.model.template.EquipmentViewVariables.SlotEntry;
import com.jefflife.mudmk2.gameplay.application.service.model.template.EquipmentViewVariables.StatDiff;
import com.jefflife.mudmk2.gameplay.application.service.model.template.EquipmentViewVariables.StatModifierLine;
import com.jefflife.mudmk2.gameplay.application.service.provided.EquipmentViewUseCase;
import com.jefflife.mudmk2.gameplay.application.service.required.ActivePlayerRepository;
import com.jefflife.mudmk2.gameplay.application.service.required.SendEquipmentViewMessagePort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class EquipmentViewCommandService implements EquipmentViewUseCase {

    private final ActivePlayerRepository players;
    private final SendEquipmentViewMessagePort sendEquipmentViewMessagePort;

    public EquipmentViewCommandService(ActivePlayerRepository players,
                                        SendEquipmentViewMessagePort sendEquipmentViewMessagePort) {
        this.players = players;
        this.sendEquipmentViewMessagePort = sendEquipmentViewMessagePort;
    }

    @Override
    public void showEquipment(EquipmentViewCommand command) {
        PlayerCharacter player = players.findByUserId(command.userId())
                .orElseThrow(() -> new PlayerNotFoundException(command.userId()));
        EquippedItems equipped = player.getEquippedItems();

        List<SlotEntry> slotEntries = new ArrayList<>();
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            slotEntries.add(buildSlotEntry(slot, equipped));
        }

        List<StatDiff> statDiffs = buildStatDiffs(player.getBaseStats(), player.getStats());

        sendEquipmentViewMessagePort.sendMessage(
                new EquipmentViewVariables(command.userId(), slotEntries, statDiffs));
    }

    private SlotEntry buildSlotEntry(EquipmentSlot slot, EquippedItems equipped) {
        String label = ItemDisplayLabels.of(slot);
        Optional<ItemInstance> opt = equipped.getSlot(slot);
        if (opt.isEmpty()) {
            return new SlotEntry(label, null, List.of());
        }
        ItemInstance inst = opt.get();
        List<StatModifierLine> mods = List.of();
        if (inst.getTemplate() instanceof EquippableItemTemplate eq) {
            mods = eq.getStatModifiers().stream()
                    .map(this::toStatLine)
                    .toList();
        }
        return new SlotEntry(label, inst.getTemplate().getName(), mods);
    }

    private StatModifierLine toStatLine(StatModifier mod) {
        return new StatModifierLine(ItemDisplayLabels.of(mod.getStatType()), mod.getValue());
    }

    private List<StatDiff> buildStatDiffs(CharacterStats base, CharacterStats eff) {
        List<StatDiff> diffs = new ArrayList<>();
        addIfDiffers(diffs, StatType.VIGOR, base.vigor(), eff.vigor());
        addIfDiffers(diffs, StatType.PHYSIQUE, base.physique(), eff.physique());
        addIfDiffers(diffs, StatType.AGILITY, base.agility(), eff.agility());
        addIfDiffers(diffs, StatType.INTELLECT, base.intellect(), eff.intellect());
        addIfDiffers(diffs, StatType.WILL, base.will(), eff.will());
        addIfDiffers(diffs, StatType.MERIDIAN, base.meridian(), eff.meridian());
        addIfDiffers(diffs, StatType.INNER_POWER, base.innerPower(), eff.innerPower());
        addIfDiffers(diffs, StatType.SPECIAL_TECHNIQUE, base.specialTechnique(), eff.specialTechnique());
        addIfDiffers(diffs, StatType.LIGHT_STEP, base.lightStep(), eff.lightStep());
        addIfDiffers(diffs, StatType.FISTS_AND_PALMS, base.fistsAndPalms(), eff.fistsAndPalms());
        addIfDiffers(diffs, StatType.SWORD_METHOD, base.swordMethod(), eff.swordMethod());
        addIfDiffers(diffs, StatType.BLADE_METHOD, base.bladeMethod(), eff.bladeMethod());
        addIfDiffers(diffs, StatType.LONG_WEAPON, base.longWeapon(), eff.longWeapon());
        addIfDiffers(diffs, StatType.ESOTERIC_WEAPON, base.esotericWeapon(), eff.esotericWeapon());
        addIfDiffers(diffs, StatType.ARCHERY, base.archery(), eff.archery());
        return diffs;
    }

    private void addIfDiffers(List<StatDiff> out, StatType type, int base, int eff) {
        if (base == eff) return;
        out.add(new StatDiff(ItemDisplayLabels.of(type), base, eff));
    }
}
