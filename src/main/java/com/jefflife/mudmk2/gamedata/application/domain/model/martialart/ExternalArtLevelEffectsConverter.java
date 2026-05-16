package com.jefflife.mudmk2.gamedata.application.domain.model.martialart;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

@Converter
public class ExternalArtLevelEffectsConverter
        implements AttributeConverter<List<ExternalArtLevelEffect>, String> {

    private static final ObjectMapper MAPPER = JsonMapper.builder().build();
    private static final TypeReference<List<ExternalArtLevelEffect>> TYPE_REF = new TypeReference<>() {};

    @Override
    public String convertToDatabaseColumn(List<ExternalArtLevelEffect> attribute) {
        if (attribute == null) return "[]";
        return MAPPER.writeValueAsString(attribute);
    }

    @Override
    public List<ExternalArtLevelEffect> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) return List.of();
        return MAPPER.readValue(dbData, TYPE_REF);
    }
}
