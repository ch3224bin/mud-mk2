package com.jefflife.mudmk2.gamedata.application.service.required;

import com.jefflife.mudmk2.gamedata.application.domain.model.martialart.LearnedMentalMethod;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LearnedMentalMethodRepository extends CrudRepository<LearnedMentalMethod, UUID> {
    boolean existsByPlayerCharacterIdAndMentalMethodTemplateId(UUID pcId, Long templateId);
    boolean existsByMentalMethodTemplateId(Long templateId);
    List<LearnedMentalMethod> findAllByPlayerCharacterId(UUID pcId);
    Optional<LearnedMentalMethod> findByIdAndPlayerCharacterId(UUID id, UUID pcId);
}
