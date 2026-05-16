package com.jefflife.mudmk2.gamedata.application.service.provided;

import com.jefflife.mudmk2.gamedata.application.domain.model.martialart.MentalMethodKind;
import com.jefflife.mudmk2.gamedata.application.domain.model.martialart.MentalMethodTemplate;

import java.util.List;

public interface MentalMethodTemplateFinder {
    MentalMethodTemplate findById(Long id);
    List<MentalMethodTemplate> findAll();
    List<MentalMethodTemplate> findByKind(MentalMethodKind kind);
    List<MentalMethodTemplate> findByNameContaining(String name);
}
