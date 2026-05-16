package com.jefflife.mudmk2.gamedata.application.service.required;

import com.jefflife.mudmk2.gamedata.application.domain.model.item.WeaponType;
import com.jefflife.mudmk2.gamedata.application.domain.model.martialart.ExternalArtTemplate;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ExternalArtTemplateRepository extends CrudRepository<ExternalArtTemplate, Long> {
    List<ExternalArtTemplate> findAllBy();
    List<ExternalArtTemplate> findByWeaponType(WeaponType weaponType);
    List<ExternalArtTemplate> findByNameContaining(String name);
}
