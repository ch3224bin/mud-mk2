package com.jefflife.mudmk2.gamedata.adapter.webapi.response;

import com.jefflife.mudmk2.gamedata.application.domain.model.item.WeaponType;
import com.jefflife.mudmk2.gamedata.application.domain.model.martialart.ExternalArtLevelEffect;
import com.jefflife.mudmk2.gamedata.application.domain.model.martialart.ExternalArtTemplate;

import java.util.List;

public record ExternalArtTemplateResponse(
        Long id, String name, String description,
        WeaponType weaponType, int maxLevel,
        List<ExternalArtLevelEffect> levelEffects
) {
    public static ExternalArtTemplateResponse from(ExternalArtTemplate t) {
        return new ExternalArtTemplateResponse(
                t.getId(), t.getName(), t.getDescription(),
                t.getWeaponType(), t.getMaxLevel(), t.getLevelEffects());
    }
}
