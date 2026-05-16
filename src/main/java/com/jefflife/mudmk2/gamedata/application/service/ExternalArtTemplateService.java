package com.jefflife.mudmk2.gamedata.application.service;

import com.jefflife.mudmk2.gamedata.application.domain.model.item.WeaponType;
import com.jefflife.mudmk2.gamedata.application.domain.model.martialart.ExternalArtTemplate;
import com.jefflife.mudmk2.gamedata.application.service.exception.MartialArtTemplateInUseException;
import com.jefflife.mudmk2.gamedata.application.service.model.request.ExternalArtTemplateRequest;
import com.jefflife.mudmk2.gamedata.application.service.provided.*;
import com.jefflife.mudmk2.gamedata.application.service.required.ExternalArtTemplateRepository;
import com.jefflife.mudmk2.gamedata.application.service.required.LearnedExternalArtRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional
public class ExternalArtTemplateService implements
        ExternalArtTemplateCreator, ExternalArtTemplateFinder,
        ExternalArtTemplateModifier, ExternalArtTemplateRemover {

    private final ExternalArtTemplateRepository repo;
    private final LearnedExternalArtRepository learnedRepo;

    public ExternalArtTemplateService(ExternalArtTemplateRepository repo,
                                      LearnedExternalArtRepository learnedRepo) {
        this.repo = repo;
        this.learnedRepo = learnedRepo;
    }

    @Override
    public ExternalArtTemplate create(ExternalArtTemplateRequest request) {
        return repo.save(ExternalArtTemplate.builder()
                .name(request.name())
                .description(request.description())
                .weaponType(request.weaponType())
                .maxLevel(request.maxLevel())
                .levelEffects(request.levelEffectsDomain())
                .build());
    }

    @Override
    @Transactional(readOnly = true)
    public ExternalArtTemplate findById(Long id) {
        return repo.findById(id).orElseThrow(
                () -> new NoSuchElementException("ExternalArtTemplate not found: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExternalArtTemplate> findAll() { return repo.findAllBy(); }

    @Override
    @Transactional(readOnly = true)
    public List<ExternalArtTemplate> findByWeaponType(WeaponType type) { return repo.findByWeaponType(type); }

    @Override
    @Transactional(readOnly = true)
    public List<ExternalArtTemplate> findByNameContaining(String name) { return repo.findByNameContaining(name); }

    @Override
    public ExternalArtTemplate update(Long id, ExternalArtTemplateRequest request) {
        ExternalArtTemplate t = repo.findById(id).orElseThrow(
                () -> new NoSuchElementException("ExternalArtTemplate not found: " + id));
        t.update(request.name(), request.description(), request.weaponType(),
                request.maxLevel(), request.levelEffectsDomain());
        return t;
    }

    @Override
    public void delete(Long id) {
        ExternalArtTemplate t = repo.findById(id).orElseThrow(
                () -> new NoSuchElementException("ExternalArtTemplate not found: " + id));
        if (learnedRepo.existsByExternalArtTemplateId(id)) {
            throw new MartialArtTemplateInUseException(
                    "ExternalArtTemplate " + id + " is in use by learned records");
        }
        repo.delete(t);
    }
}
