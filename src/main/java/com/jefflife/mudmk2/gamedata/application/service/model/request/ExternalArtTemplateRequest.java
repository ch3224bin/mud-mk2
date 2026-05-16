package com.jefflife.mudmk2.gamedata.application.service.model.request;

import com.jefflife.mudmk2.gamedata.application.domain.model.item.WeaponType;
import com.jefflife.mudmk2.gamedata.application.domain.model.martialart.ExternalArtLevelEffect;

import java.util.List;

public record ExternalArtTemplateRequest(
        String name,
        String description,
        WeaponType weaponType,
        int maxLevel,
        List<ExternalArtLevelEffectRequest> levelEffects
) {
    public List<ExternalArtLevelEffect> levelEffectsDomain() {
        return levelEffects == null ? List.of()
                : levelEffects.stream().map(ExternalArtLevelEffectRequest::toDomain).toList();
    }
}
