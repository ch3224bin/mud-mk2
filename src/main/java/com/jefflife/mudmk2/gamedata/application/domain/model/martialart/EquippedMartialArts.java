package com.jefflife.mudmk2.gamedata.application.domain.model.martialart;

import com.jefflife.mudmk2.gamedata.application.service.exception.MartialArtSlotFullException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Entity
@Table(name = "equipped_martial_arts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EquippedMartialArts {

    public static final int EXTERNAL_SLOT_MAX = 6;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ElementCollection
    @CollectionTable(name = "equipped_mental_slot",
                     joinColumns = @JoinColumn(name = "equipped_martial_arts_id"))
    @MapKeyEnumerated(EnumType.STRING)
    @MapKeyColumn(name = "kind")
    @Column(name = "learned_mental_method_id")
    private Map<MentalMethodKind, UUID> mentalSlots = new EnumMap<>(MentalMethodKind.class);

    @ElementCollection
    @CollectionTable(name = "equipped_external_slot",
                     joinColumns = @JoinColumn(name = "equipped_martial_arts_id"))
    @Column(name = "learned_external_art_id")
    private List<UUID> externalSlots = new ArrayList<>();

    public static EquippedMartialArts create() {
        return new EquippedMartialArts();
    }

    public void equipMental(MentalMethodKind kind, UUID learnedId) {
        mentalSlots.put(kind, learnedId);
    }

    public Optional<UUID> unequipMental(MentalMethodKind kind) {
        return Optional.ofNullable(mentalSlots.remove(kind));
    }

    public void equipExternal(UUID learnedId) {
        if (externalSlots.contains(learnedId)) return;
        if (externalSlots.size() >= EXTERNAL_SLOT_MAX) {
            throw new MartialArtSlotFullException(
                    "external slot full (max " + EXTERNAL_SLOT_MAX + ")");
        }
        externalSlots.add(learnedId);
    }

    public boolean unequipExternal(UUID learnedId) {
        return externalSlots.remove(learnedId);
    }

    public void initializeAssociatedEntities() {
        mentalSlots.size();
        externalSlots.size();
    }
}
