package com.jefflife.mudmk2.gamedata.application.service;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.NonPlayerCharacter;
import com.jefflife.mudmk2.gamedata.application.service.required.NonPlayerCharacterRepository;
import com.jefflife.mudmk2.gamedata.application.service.provided.NonPlayerCharacterCreator;
import com.jefflife.mudmk2.gamedata.application.service.provided.NonPlayerCharacterRemover;
import com.jefflife.mudmk2.gamedata.application.service.provided.NonPlayerCharacterFinder;
import com.jefflife.mudmk2.gamedata.application.service.provided.NonPlayerCharacterModifier;
import com.jefflife.mudmk2.gamedata.application.service.model.request.CreateNonPlayerCharacterRequest;
import com.jefflife.mudmk2.gamedata.application.service.model.request.UpdateNonPlayerCharacterRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class NonPlayerCharacterService implements
        NonPlayerCharacterCreator,
        NonPlayerCharacterFinder,
        NonPlayerCharacterModifier,
        NonPlayerCharacterRemover {

    private final NonPlayerCharacterRepository nonPlayerCharacterRepository;

    public NonPlayerCharacterService(NonPlayerCharacterRepository nonPlayerCharacterRepository) {
        this.nonPlayerCharacterRepository = nonPlayerCharacterRepository;
    }

    @Transactional
    @Override
    public NonPlayerCharacter createNonPlayerCharacter(final CreateNonPlayerCharacterRequest createNonPlayerCharacterRequest) {
        final NonPlayerCharacter nonPlayerCharacter = createNonPlayerCharacterRequest.toDomain();
        return nonPlayerCharacterRepository.save(nonPlayerCharacter);
    }

    @Transactional(readOnly = true)
    @Override
    public NonPlayerCharacter getNonPlayerCharacter(final UUID id) {
        return nonPlayerCharacterRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("NonPlayerCharacter not found with id: " + id));
    }

    @Transactional(readOnly = true)
    @Override
    public List<NonPlayerCharacter> getAllNonPlayerCharacters() {
        return nonPlayerCharacterRepository.findAll();
    }

    @Transactional
    @Override
    public NonPlayerCharacter updateNonPlayerCharacter(final UUID id, final UpdateNonPlayerCharacterRequest updateNonPlayerCharacterRequest) {
        if (!nonPlayerCharacterRepository.existsById(id)) {
            throw new NoSuchElementException("NonPlayerCharacter not found with id: " + id);
        }

        final NonPlayerCharacter nonPlayerCharacter = updateNonPlayerCharacterRequest.toDomain(id);
        return nonPlayerCharacterRepository.save(nonPlayerCharacter);
    }

    @Transactional
    @Override
    public void deleteNonPlayerCharacter(final UUID id) {
        if (!nonPlayerCharacterRepository.existsById(id)) {
            throw new NoSuchElementException("NonPlayerCharacter not found with id: " + id);
        }

        nonPlayerCharacterRepository.deleteById(id);
    }
}
