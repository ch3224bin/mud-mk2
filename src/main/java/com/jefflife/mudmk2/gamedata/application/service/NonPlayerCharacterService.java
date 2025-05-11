package com.jefflife.mudmk2.gamedata.application.service;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.NonPlayerCharacter;
import com.jefflife.mudmk2.gamedata.application.domain.repository.NonPlayerCharacterRepository;
import com.jefflife.mudmk2.gamedata.application.port.in.CreateNonPlayerCharacterUseCase;
import com.jefflife.mudmk2.gamedata.application.service.model.request.CreateNonPlayerCharacterRequest;
import com.jefflife.mudmk2.gamedata.application.service.model.response.NonPlayerCharacterResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NonPlayerCharacterService implements CreateNonPlayerCharacterUseCase {
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
}
