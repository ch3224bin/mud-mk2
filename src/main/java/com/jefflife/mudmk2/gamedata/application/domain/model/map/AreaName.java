package com.jefflife.mudmk2.gamedata.application.domain.model.map;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static org.apache.commons.lang3.StringUtils.*;

@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class AreaName {
    @Column(nullable = false)
    private String name;

    public static AreaName of(String name) {
        String trimmedName = trimToNull(name);
        validateAreaName(trimmedName);

        AreaName areaName = new AreaName();

        areaName.name = trimmedName;

        return areaName;
    }

    private static void validateAreaName(String name) {
        if (isBlank(name) || name.length() > 255) {
            throw new InvalidAreaNameException("Area name must be between 1 and 255 characters");
        }
    }
}
