package com.jefflife.mudmk2.gamedata.application.service;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.NonPlayerCharacter;
import com.jefflife.mudmk2.gamedata.application.service.required.NonPlayerCharacterRepository;
import com.jefflife.mudmk2.gamedata.application.service.provided.NonPlayerCharacterCreator;
import com.jefflife.mudmk2.gamedata.application.service.provided.NonPlayerCharacterRemover;
import com.jefflife.mudmk2.gamedata.application.service.provided.NonPlayerCharacterFinder;
import com.jefflife.mudmk2.gamedata.application.service.provided.NonPlayerCharacterModifier;
import com.jefflife.mudmk2.gamedata.application.service.model.request.CreateNonPlayerCharacterRequest;
import com.jefflife.mudmk2.gamedata.application.service.model.request.UpdateNonPlayerCharacterRequest;
import com.jefflife.mudmk2.gamedata.application.service.model.response.NonPlayerCharacterResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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

    public NonPlayerCharacterService(final NonPlayerCharacterRepository nonPlayerCharacterRepository) {
        this.nonPlayerCharacterRepository = nonPlayerCharacterRepository;
    }

    @Transactional
    @Override
    public NonPlayerCharacterResponse createNonPlayerCharacter(final CreateNonPlayerCharacterRequest createNonPlayerCharacterRequest) {
        final NonPlayerCharacter nonPlayerCharacter = createNonPlayerCharacterRequest.toDomain();
        final NonPlayerCharacter savedNonPlayerCharacter = nonPlayerCharacterRepository.save(nonPlayerCharacter);
        return NonPlayerCharacterResponse.of(savedNonPlayerCharacter);
    }

    @Transactional(readOnly = true)
    @Override
    public NonPlayerCharacterResponse getNonPlayerCharacter(final UUID id) {
        final NonPlayerCharacter nonPlayerCharacter = nonPlayerCharacterRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("NonPlayerCharacter not found with id: " + id));
        return NonPlayerCharacterResponse.of(nonPlayerCharacter);
    }

    @Transactional(readOnly = true)
    @Override
    public List<NonPlayerCharacterResponse> getAllNonPlayerCharacters() {
        final List<NonPlayerCharacterResponse> responses = new ArrayList<>();
        nonPlayerCharacterRepository.findAll().forEach(npc -> responses.add(NonPlayerCharacterResponse.of(npc)));
        return responses;
    }

    @Transactional
    @Override
    public NonPlayerCharacterResponse updateNonPlayerCharacter(final UUID id, final UpdateNonPlayerCharacterRequest updateNonPlayerCharacterRequest) {
        if (!nonPlayerCharacterRepository.existsById(id)) {
            throw new NoSuchElementException("NonPlayerCharacter not found with id: " + id);
        }

        final NonPlayerCharacter nonPlayerCharacter = updateNonPlayerCharacterRequest.toDomain(id);
        final NonPlayerCharacter savedNonPlayerCharacter = nonPlayerCharacterRepository.save(nonPlayerCharacter);
        return NonPlayerCharacterResponse.of(savedNonPlayerCharacter);
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
