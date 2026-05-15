package com.jefflife.mudmk2.gamedata.application.domain.model.player;

import com.jefflife.mudmk2.gamedata.application.domain.model.item.EquipmentSlot;
import com.jefflife.mudmk2.gamedata.application.domain.model.item.EquippableItemTemplate;
import com.jefflife.mudmk2.gamedata.application.domain.model.item.ItemInstance;
import com.jefflife.mudmk2.gamedata.application.domain.model.item.StatModifier;
import com.jefflife.mudmk2.gamedata.application.domain.model.item.StatType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Entity
@Table(name = "equipped_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EquippedItems {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "equipped_items_id")
    @MapKeyEnumerated(EnumType.STRING)
    @MapKeyColumn(name = "equipped_slot")
    private Map<EquipmentSlot, ItemInstance> slots = new EnumMap<>(EquipmentSlot.class);

    private EquippedItems(Map<EquipmentSlot, ItemInstance> slots) {
        this.slots = slots;
    }

    public static EquippedItems create() {
        return new EquippedItems(new EnumMap<>(EquipmentSlot.class));
    }

    public Optional<ItemInstance> equip(EquipmentSlot slot, ItemInstance instance) {
        ItemInstance previous = slots.put(slot, instance);
        return Optional.ofNullable(previous);
    }

    public Optional<ItemInstance> unequip(EquipmentSlot slot) {
        return Optional.ofNullable(slots.remove(slot));
    }

    public Optional<ItemInstance> getSlot(EquipmentSlot slot) {
        return Optional.ofNullable(slots.get(slot));
    }

    public Optional<Map.Entry<EquipmentSlot, ItemInstance>> findByItemName(String name) {
        return slots.entrySet().stream()
                .filter(e -> e.getValue().getTemplate().getName().equals(name))
                .findFirst();
    }

    public Map<StatType, Integer> sumStatModifiers() {
        Map<StatType, Integer> sums = new HashMap<>();
        for (ItemInstance instance : slots.values()) {
            if (instance.getTemplate() instanceof EquippableItemTemplate equip) {
                for (StatModifier mod : equip.getStatModifiers()) {
                    sums.merge(mod.getStatType(), mod.getValue(), Integer::sum);
                }
            }
        }
        return sums;
    }

    public void initializeAssociatedEntities() {
        this.slots.size();
        for (ItemInstance instance : slots.values()) {
            instance.initializeAssociatedEntities();
        }
    }
}
