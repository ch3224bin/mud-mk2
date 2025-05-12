package com.jefflife.mudmk2.gamedata.adapter.in;

import com.jefflife.mudmk2.gamedata.application.port.in.CreateNonPlayerCharacterUseCase;
import com.jefflife.mudmk2.gamedata.application.port.in.DeleteNonPlayerCharacterUseCase;
import com.jefflife.mudmk2.gamedata.application.port.in.GetNonPlayerCharacterUseCase;
import com.jefflife.mudmk2.gamedata.application.port.in.UpdateNonPlayerCharacterUseCase;
import com.jefflife.mudmk2.gamedata.application.service.model.request.CreateNonPlayerCharacterRequest;
import com.jefflife.mudmk2.gamedata.application.service.model.request.UpdateNonPlayerCharacterRequest;
import com.jefflife.mudmk2.gamedata.application.service.model.response.NonPlayerCharacterResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping(NonPlayerCharacterController.BASE_PATH)
public class NonPlayerCharacterController {
    public static final String BASE_PATH = "/api/v1/npcs";

    private final CreateNonPlayerCharacterUseCase createNonPlayerCharacterUseCase;
    private final GetNonPlayerCharacterUseCase getNonPlayerCharacterUseCase;
    private final UpdateNonPlayerCharacterUseCase updateNonPlayerCharacterUseCase;
    private final DeleteNonPlayerCharacterUseCase deleteNonPlayerCharacterUseCase;

    public NonPlayerCharacterController(
            final CreateNonPlayerCharacterUseCase createNonPlayerCharacterUseCase,
            final GetNonPlayerCharacterUseCase getNonPlayerCharacterUseCase,
            final UpdateNonPlayerCharacterUseCase updateNonPlayerCharacterUseCase,
            final DeleteNonPlayerCharacterUseCase deleteNonPlayerCharacterUseCase
    ) {
        this.createNonPlayerCharacterUseCase = createNonPlayerCharacterUseCase;
        this.getNonPlayerCharacterUseCase = getNonPlayerCharacterUseCase;
        this.updateNonPlayerCharacterUseCase = updateNonPlayerCharacterUseCase;
        this.deleteNonPlayerCharacterUseCase = deleteNonPlayerCharacterUseCase;
    }

    @PostMapping
    public ResponseEntity<NonPlayerCharacterResponse> createNonPlayerCharacter(
            @RequestBody final CreateNonPlayerCharacterRequest createNonPlayerCharacterRequest
    ) {
        final NonPlayerCharacterResponse nonPlayerCharacterResponse = createNonPlayerCharacterUseCase.createNonPlayerCharacter(createNonPlayerCharacterRequest);
        return ResponseEntity
                .created(URI.create(String.format("%s/%s", BASE_PATH, nonPlayerCharacterResponse.id())))
                .body(nonPlayerCharacterResponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<NonPlayerCharacterResponse> getNonPlayerCharacter(@PathVariable final Long id) {
        try {
            final NonPlayerCharacterResponse nonPlayerCharacterResponse = getNonPlayerCharacterUseCase.getNonPlayerCharacter(id);
            return ResponseEntity.ok(nonPlayerCharacterResponse);
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<NonPlayerCharacterResponse>> getAllNonPlayerCharacters() {
        final List<NonPlayerCharacterResponse> nonPlayerCharacterResponses = getNonPlayerCharacterUseCase.getAllNonPlayerCharacters();
        return ResponseEntity.ok(nonPlayerCharacterResponses);
    }

    @PutMapping("/{id}")
    public ResponseEntity<NonPlayerCharacterResponse> updateNonPlayerCharacter(
            @PathVariable final Long id,
            @RequestBody final UpdateNonPlayerCharacterRequest updateNonPlayerCharacterRequest
    ) {
        try {
            final NonPlayerCharacterResponse nonPlayerCharacterResponse = updateNonPlayerCharacterUseCase.updateNonPlayerCharacter(id, updateNonPlayerCharacterRequest);
            return ResponseEntity.ok(nonPlayerCharacterResponse);
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNonPlayerCharacter(@PathVariable final Long id) {
        try {
            deleteNonPlayerCharacterUseCase.deleteNonPlayerCharacter(id);
            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
