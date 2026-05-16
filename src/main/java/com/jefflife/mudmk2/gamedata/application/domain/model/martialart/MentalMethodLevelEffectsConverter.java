package com.jefflife.mudmk2.gamedata.application.domain.model.martialart;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

@Converter
public class MentalMethodLevelEffectsConverter
        implements AttributeConverter<List<MentalMethodLevelEffect>, String> {

    private static final ObjectMapper MAPPER = JsonMapper.builder()
            .changeDefaultVisibility(vc -> vc.withVisibility(PropertyAccessor.FIELD, Visibility.ANY))
            .build();
    private static final TypeReference<List<MentalMethodLevelEffect>> TYPE_REF = new TypeReference<>() {};

    @Override
    public String convertToDatabaseColumn(List<MentalMethodLevelEffect> attribute) {
        if (attribute == null) return "[]";
        return MAPPER.writeValueAsString(attribute);
    }

    @Override
    public List<MentalMethodLevelEffect> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) return List.of();
        return MAPPER.readValue(dbData, TYPE_REF);
    }
}
