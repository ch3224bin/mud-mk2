package com.jefflife.mudmk2.gamedata.application.domain.model.martialart;

import com.jefflife.mudmk2.gamedata.application.domain.model.martialart.MartialArtSlotFullException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EquippedMartialArtsTest {

    @Test
    void equipMental_putsInKindSlot() {
        EquippedMartialArts e = EquippedMartialArts.create();
        UUID learned = UUID.randomUUID();

        e.equipMental(MentalMethodKind.INNER_POWER, learned);

        assertThat(e.getMentalSlots()).containsEntry(MentalMethodKind.INNER_POWER, learned);
    }

    @Test
    void equipMental_sameKindAgain_replacesPrevious() {
        EquippedMartialArts e = EquippedMartialArts.create();
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();

        e.equipMental(MentalMethodKind.INNER_POWER, first);
        e.equipMental(MentalMethodKind.INNER_POWER, second);

        assertThat(e.getMentalSlots()).containsEntry(MentalMethodKind.INNER_POWER, second);
        assertThat(e.getMentalSlots()).hasSize(1);
    }

    @Test
    void unequipMental_removesAndReturnsPrevious() {
        EquippedMartialArts e = EquippedMartialArts.create();
        UUID learned = UUID.randomUUID();
        e.equipMental(MentalMethodKind.INNER_POWER, learned);

        assertThat(e.unequipMental(MentalMethodKind.INNER_POWER)).contains(learned);
        assertThat(e.getMentalSlots()).doesNotContainKey(MentalMethodKind.INNER_POWER);
    }

    @Test
    void equipExternal_addsToSlots() {
        EquippedMartialArts e = EquippedMartialArts.create();
        UUID learned = UUID.randomUUID();

        e.equipExternal(learned);

        assertThat(e.getExternalSlots()).containsExactly(learned);
    }

    @Test
    void equipExternal_duplicateIsNoOp() {
        EquippedMartialArts e = EquippedMartialArts.create();
        UUID learned = UUID.randomUUID();

        e.equipExternal(learned);
        e.equipExternal(learned);

        assertThat(e.getExternalSlots()).containsExactly(learned);
    }

    @Test
    void equipExternal_whenFull_throws() {
        EquippedMartialArts e = EquippedMartialArts.create();
        for (int i = 0; i < EquippedMartialArts.EXTERNAL_SLOT_MAX; i++) {
            e.equipExternal(UUID.randomUUID());
        }

        assertThatThrownBy(() -> e.equipExternal(UUID.randomUUID()))
                .isInstanceOf(MartialArtSlotFullException.class);
    }

    @Test
    void unequipExternal_removesById_returnsTrue() {
        EquippedMartialArts e = EquippedMartialArts.create();
        UUID a = UUID.randomUUID();
        UUID b = UUID.randomUUID();
        e.equipExternal(a);
        e.equipExternal(b);

        assertThat(e.unequipExternal(a)).isTrue();
        assertThat(e.getExternalSlots()).containsExactly(b);
    }

    @Test
    void unequipExternal_notPresent_returnsFalse() {
        EquippedMartialArts e = EquippedMartialArts.create();
        assertThat(e.unequipExternal(UUID.randomUUID())).isFalse();
    }
}
