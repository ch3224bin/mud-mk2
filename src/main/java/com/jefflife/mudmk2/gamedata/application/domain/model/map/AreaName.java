package com.jefflife.mudmk2.gamedata.application.domain.model.map;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class AreaName {
    private static final String VALID_AREA_NAME_PATTERN = "^[a-zA-Z0-9가-힣]+$";

    @Column(nullable = false)
    private String name;

    public static AreaName of(String name) {
        validateAreaName(name);

        AreaName areaName = new AreaName();

        areaName.name = name;

        return areaName;
    }

    private static void validateAreaName(String name) {
        if (StringUtils.isBlank(name) || name.length() > 255) {
            throw new InvalidAreaNameException("Area name must be between 1 and 255 characters");
        }

        if (!name.matches(VALID_AREA_NAME_PATTERN)) {
            throw new InvalidAreaNameException("Area name can only contain letters, numbers, and Korean characters");
        }
    }
}
