package com.jefflife.mudmk2.gamedata.application.service;

import com.jefflife.mudmk2.gamedata.application.domain.model.martialart.MentalMethodKind;
import com.jefflife.mudmk2.gamedata.application.domain.model.martialart.MentalMethodTemplate;
import com.jefflife.mudmk2.gamedata.application.service.exception.MartialArtTemplateInUseException;
import com.jefflife.mudmk2.gamedata.application.service.model.request.MentalMethodTemplateRequest;
import com.jefflife.mudmk2.gamedata.application.service.provided.*;
import com.jefflife.mudmk2.gamedata.application.service.required.LearnedMentalMethodRepository;
import com.jefflife.mudmk2.gamedata.application.service.required.MentalMethodTemplateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional
public class MentalMethodTemplateService implements
        MentalMethodTemplateCreator, MentalMethodTemplateFinder,
        MentalMethodTemplateModifier, MentalMethodTemplateRemover {

    private final MentalMethodTemplateRepository repo;
    private final LearnedMentalMethodRepository learnedRepo;

    public MentalMethodTemplateService(MentalMethodTemplateRepository repo,
                                       LearnedMentalMethodRepository learnedRepo) {
        this.repo = repo;
        this.learnedRepo = learnedRepo;
    }

    @Override
    public MentalMethodTemplate create(MentalMethodTemplateRequest request) {
        MentalMethodTemplate template = MentalMethodTemplate.builder()
                .name(request.name())
                .description(request.description())
                .kind(request.kind())
                .maxLevel(request.maxLevel())
                .levelEffects(request.levelEffectsDomain())
                .build();
        return repo.save(template);
    }

    @Override
    @Transactional(readOnly = true)
    public MentalMethodTemplate findById(Long id) {
        return repo.findById(id).orElseThrow(
                () -> new NoSuchElementException("MentalMethodTemplate not found: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MentalMethodTemplate> findAll() { return repo.findAllBy(); }

    @Override
    @Transactional(readOnly = true)
    public List<MentalMethodTemplate> findByKind(MentalMethodKind kind) { return repo.findByKind(kind); }

    @Override
    @Transactional(readOnly = true)
    public List<MentalMethodTemplate> findByNameContaining(String name) { return repo.findByNameContaining(name); }

    @Override
    public MentalMethodTemplate update(Long id, MentalMethodTemplateRequest request) {
        MentalMethodTemplate t = repo.findById(id).orElseThrow(
                () -> new NoSuchElementException("MentalMethodTemplate not found: " + id));
        t.update(request.name(), request.description(), request.kind(),
                request.maxLevel(), request.levelEffectsDomain());
        return t;
    }

    @Override
    public void delete(Long id) {
        MentalMethodTemplate t = repo.findById(id).orElseThrow(
                () -> new NoSuchElementException("MentalMethodTemplate not found: " + id));
        if (learnedRepo.existsByMentalMethodTemplateId(id)) {
            throw new MartialArtTemplateInUseException(
                    "MentalMethodTemplate " + id + " is in use by learned records");
        }
        repo.delete(t);
    }
}
