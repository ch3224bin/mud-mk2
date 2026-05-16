package com.jefflife.mudmk2.gamedata.application.service.provided;

import com.jefflife.mudmk2.gamedata.application.domain.model.item.WeaponType;
import com.jefflife.mudmk2.gamedata.application.domain.model.martialart.ExternalArtTemplate;

import java.util.List;

public interface ExternalArtTemplateFinder {
    ExternalArtTemplate findById(Long id);
    List<ExternalArtTemplate> findAll();
    List<ExternalArtTemplate> findByWeaponType(WeaponType weaponType);
    List<ExternalArtTemplate> findByNameContaining(String name);
}
