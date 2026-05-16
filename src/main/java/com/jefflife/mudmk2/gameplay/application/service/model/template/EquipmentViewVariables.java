package com.jefflife.mudmk2.gameplay.application.service.model.template;

import java.util.List;

public record EquipmentViewVariables(
        Long userId,
        List<SlotEntry> slots,
        List<StatDiff> statDiffs
) {
    public record SlotEntry(
            String slotLabel,
            String itemName,   // null = empty slot, template shows "(없음)"
            List<StatModifierLine> modifiers
    ) {}

    public record StatModifierLine(String label, int value) {}

    public record StatDiff(String label, int base, int effective) {}
}
