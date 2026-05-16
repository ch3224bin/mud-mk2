package com.jefflife.mudmk2.gamedata.application.service.required;

import com.jefflife.mudmk2.gamedata.application.domain.model.martialart.LearnedExternalArt;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LearnedExternalArtRepository extends CrudRepository<LearnedExternalArt, UUID> {
    boolean existsByPlayerCharacterIdAndExternalArtTemplateId(UUID pcId, Long templateId);
    boolean existsByExternalArtTemplateId(Long templateId);
    List<LearnedExternalArt> findAllByPlayerCharacterId(UUID pcId);
    Optional<LearnedExternalArt> findByIdAndPlayerCharacterId(UUID id, UUID pcId);
}
