package com.jefflife.mudmk2.gamedata.application.domain.model.martialart;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExternalArtLevelEffectsConverterTest {

    private final ExternalArtLevelEffectsConverter converter = new ExternalArtLevelEffectsConverter();

    @Test
    void roundTrip_preservesAllFields() {
        List<ExternalArtLevelEffect> input = List.of(
                new ExternalArtLevelEffect(1, 1.1, 6, 5, 0),
                new ExternalArtLevelEffect(2, 1.25, 5, 5, 3)
        );

        String json = converter.convertToDatabaseColumn(input);
        List<ExternalArtLevelEffect> restored = converter.convertToEntityAttribute(json);

        assertThat(restored).isEqualTo(input);
    }

    @Test
    void recordValidation_rejectsNegativeValues() {
        assertThatThrownBy(() -> new ExternalArtLevelEffect(1, -0.1, 5, 5, 0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new ExternalArtLevelEffect(1, 1.0, -1, 5, 0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new ExternalArtLevelEffect(1, 1.0, 5, -1, 0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new ExternalArtLevelEffect(1, 1.0, 5, 5, -1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void null_or_blank_roundsTripToEmptyList() {
        assertThat(converter.convertToEntityAttribute(null)).isEmpty();
        assertThat(converter.convertToEntityAttribute("")).isEmpty();
        assertThat(converter.convertToDatabaseColumn(null)).isEqualTo("[]");
    }
}
