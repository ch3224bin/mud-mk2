package com.jefflife.mudmk2.gamedata.application.domain.model.martialart;

import com.jefflife.mudmk2.gamedata.application.domain.model.item.StatModifier;
import com.jefflife.mudmk2.gamedata.application.domain.model.item.StatType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MentalMethodLevelEffectsConverterTest {

    private final MentalMethodLevelEffectsConverter converter = new MentalMethodLevelEffectsConverter();

    @Test
    void roundTrip_preservesLevelsAndModifiers() {
        List<MentalMethodLevelEffect> input = List.of(
                new MentalMethodLevelEffect(1, List.of(new StatModifier(StatType.INNER_POWER, 5))),
                new MentalMethodLevelEffect(2, List.of(
                        new StatModifier(StatType.INNER_POWER, 10),
                        new StatModifier(StatType.MERIDIAN, 2)))
        );

        String json = converter.convertToDatabaseColumn(input);
        List<MentalMethodLevelEffect> restored = converter.convertToEntityAttribute(json);

        assertThat(restored).isEqualTo(input);
    }

    @Test
    void null_roundsTripToEmptyList() {
        assertThat(converter.convertToDatabaseColumn(null)).isEqualTo("[]");
        assertThat(converter.convertToEntityAttribute(null)).isEmpty();
        assertThat(converter.convertToEntityAttribute("")).isEmpty();
    }
}
