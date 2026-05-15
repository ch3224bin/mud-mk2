package com.jefflife.mudmk2.gamedata.application.domain.model.item;

import java.util.List;

/**
 * Marker interface for ItemTemplates that can be equipped.
 * Equipped templates expose their stat modifiers; target slot resolution is delegated
 * to the equip service because RING accessories choose between RING_LEFT/RING_RIGHT
 * based on current EquippedItems state.
 */
public interface EquippableItemTemplate {
    List<StatModifier> getStatModifiers();
}
