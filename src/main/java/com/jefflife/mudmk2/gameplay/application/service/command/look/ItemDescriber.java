package com.jefflife.mudmk2.gameplay.application.service.command.look;

import com.jefflife.mudmk2.gamedata.application.domain.model.item.*;
import com.jefflife.mudmk2.gameplay.application.service.model.template.ItemInfoVariables;
import com.jefflife.mudmk2.gameplay.application.service.model.template.ItemInfoVariables.StatModifierLine;
import com.jefflife.mudmk2.gameplay.application.service.required.SendItemInfoMessagePort;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ItemDescriber implements LookTargetDescriber {

    private final SendItemInfoMessagePort sendItemInfoMessagePort;

    public ItemDescriber(SendItemInfoMessagePort sendItemInfoMessagePort) {
        this.sendItemInfoMessagePort = sendItemInfoMessagePort;
    }

    @Override
    public void describe(Long userId, Lookable target) {
        ItemLookable il = (ItemLookable) target;
        ItemInstance instance = il.instance();
        ItemTemplate template = instance.getTemplate();

        String location = il.location() == ItemLookable.ItemLocation.ROOM ? "바닥" : "소지품";
        String typeLabel = buildTypeLabel(template);

        boolean hasRecovery = false;
        int hp = 0, mp = 0, ap = 0;
        List<StatModifierLine> stats = List.of();
        String skillRef = null;
        String missionInfo = null;

        if (template instanceof FoodTemplate f) {
            hp = f.getHpRecovery();
            mp = f.getMpRecovery();
            ap = f.getApRecovery();
            hasRecovery = hp > 0 || mp > 0 || ap > 0;
        } else if (template instanceof WeaponTemplate w) {
            stats = toStatLines(w.getStatModifiers());
        } else if (template instanceof EquipmentTemplate e) {
            stats = toStatLines(e.getStatModifiers());
        } else if (template instanceof AccessoryTemplate a) {
            stats = toStatLines(a.getStatModifiers());
        } else if (template instanceof MartialArtsBookTemplate m) {
            skillRef = m.getSkillRef();
        } else if (template instanceof MissionItemTemplate mi) {
            missionInfo = ItemDisplayLabels.of(mi.getMissionItemType()) + " / " + mi.getTargetRef();
        }

        sendItemInfoMessagePort.sendMessage(new ItemInfoVariables(
                userId,
                template.getName(),
                template.getDescription(),
                location,
                typeLabel,
                template.getWeight(),
                instance.getQuantity(),
                template.isStackable(),
                hasRecovery, hp, mp, ap,
                stats,
                skillRef,
                missionInfo
        ));
    }

    @Override
    public LookableType getLookableType() {
        return LookableType.ITEM;
    }

    private String buildTypeLabel(ItemTemplate template) {
        String base = ItemDisplayLabels.of(template.getItemType());
        if (template instanceof WeaponTemplate w) {
            return base + "(" + ItemDisplayLabels.of(w.getWeaponType()) + ")";
        }
        if (template instanceof EquipmentTemplate e) {
            return base + "(" + ItemDisplayLabels.of(e.getEquipmentSlot()) + ")";
        }
        if (template instanceof AccessoryTemplate a) {
            return base + "(" + ItemDisplayLabels.of(a.getAccessoryType()) + ")";
        }
        return base;
    }

    private List<StatModifierLine> toStatLines(List<StatModifier> mods) {
        return mods.stream()
                .map(sm -> new StatModifierLine(ItemDisplayLabels.of(sm.getStatType()), sm.getValue()))
                .toList();
    }
}
