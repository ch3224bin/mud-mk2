package com.jefflife.mudmk2.gamedata.application.service.required;

import com.jefflife.mudmk2.gamedata.application.domain.model.martialart.MentalMethodKind;
import com.jefflife.mudmk2.gamedata.application.domain.model.martialart.MentalMethodTemplate;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface MentalMethodTemplateRepository extends CrudRepository<MentalMethodTemplate, Long> {
    List<MentalMethodTemplate> findAllBy();
    List<MentalMethodTemplate> findByKind(MentalMethodKind kind);
    List<MentalMethodTemplate> findByNameContaining(String name);
}
