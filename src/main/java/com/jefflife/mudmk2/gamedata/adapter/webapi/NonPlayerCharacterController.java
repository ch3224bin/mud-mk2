package com.jefflife.mudmk2.gamedata.adapter.webapi;

import com.jefflife.mudmk2.gamedata.application.service.provided.NonPlayerCharacterCreator;
import com.jefflife.mudmk2.gamedata.application.service.provided.NonPlayerCharacterRemover;
import com.jefflife.mudmk2.gamedata.application.service.provided.NonPlayerCharacterFinder;
import com.jefflife.mudmk2.gamedata.application.service.provided.NonPlayerCharacterModifier;
import com.jefflife.mudmk2.gamedata.application.service.model.request.CreateNonPlayerCharacterRequest;
import com.jefflife.mudmk2.gamedata.application.service.model.request.UpdateNonPlayerCharacterRequest;
import com.jefflife.mudmk2.gamedata.adapter.webapi.response.NonPlayerCharacterResponse;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.NonPlayerCharacter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@RestController
@RequestMapping(NonPlayerCharacterController.BASE_PATH)
public class NonPlayerCharacterController {
    public static final String BASE_PATH = "/api/v1/npcs";

    private final NonPlayerCharacterCreator nonPlayerCharacterCreator;
    private final NonPlayerCharacterFinder nonPlayerCharacterFinder;
    private final NonPlayerCharacterModifier nonPlayerCharacterModifier;
    private final NonPlayerCharacterRemover nonPlayerCharacterRemover;

    public NonPlayerCharacterController(
            final NonPlayerCharacterCreator nonPlayerCharacterCreator,
            final NonPlayerCharacterFinder nonPlayerCharacterFinder,
            final NonPlayerCharacterModifier nonPlayerCharacterModifier,
            final NonPlayerCharacterRemover nonPlayerCharacterRemover
    ) {
        this.nonPlayerCharacterCreator = nonPlayerCharacterCreator;
        this.nonPlayerCharacterFinder = nonPlayerCharacterFinder;
        this.nonPlayerCharacterModifier = nonPlayerCharacterModifier;
        this.nonPlayerCharacterRemover = nonPlayerCharacterRemover;
    }

    @PostMapping
    public ResponseEntity<NonPlayerCharacterResponse> createNonPlayerCharacter(
            @RequestBody final CreateNonPlayerCharacterRequest createNonPlayerCharacterRequest
    ) {
        final NonPlayerCharacter nonPlayerCharacter = nonPlayerCharacterCreator.createNonPlayerCharacter(createNonPlayerCharacterRequest);
        final NonPlayerCharacterResponse nonPlayerCharacterResponse = NonPlayerCharacterResponse.of(nonPlayerCharacter);
        return ResponseEntity
                .created(URI.create(String.format("%s/%s", BASE_PATH, nonPlayerCharacterResponse.id())))
                .body(nonPlayerCharacterResponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<NonPlayerCharacterResponse> getNonPlayerCharacter(@PathVariable final UUID id) {
        try {
            final NonPlayerCharacter nonPlayerCharacter = nonPlayerCharacterFinder.getNonPlayerCharacter(id);
            final NonPlayerCharacterResponse nonPlayerCharacterResponse = NonPlayerCharacterResponse.of(nonPlayerCharacter);
            return ResponseEntity.ok(nonPlayerCharacterResponse);
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<NonPlayerCharacterResponse>> getAllNonPlayerCharacters() {
        final List<NonPlayerCharacter> nonPlayerCharacters = nonPlayerCharacterFinder.getAllNonPlayerCharacters();
        final List<NonPlayerCharacterResponse> nonPlayerCharacterResponses = nonPlayerCharacters.stream()
                .map(NonPlayerCharacterResponse::of)
                .toList();
        return ResponseEntity.ok(nonPlayerCharacterResponses);
    }

    @PutMapping("/{id}")
    public ResponseEntity<NonPlayerCharacterResponse> updateNonPlayerCharacter(
            @PathVariable final UUID id,
            @RequestBody final UpdateNonPlayerCharacterRequest updateNonPlayerCharacterRequest
    ) {
        try {
            final NonPlayerCharacter nonPlayerCharacter = nonPlayerCharacterModifier.updateNonPlayerCharacter(id, updateNonPlayerCharacterRequest);
            final NonPlayerCharacterResponse nonPlayerCharacterResponse = NonPlayerCharacterResponse.of(nonPlayerCharacter);
            return ResponseEntity.ok(nonPlayerCharacterResponse);
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNonPlayerCharacter(@PathVariable final UUID id) {
        try {
            nonPlayerCharacterRemover.deleteNonPlayerCharacter(id);
            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
