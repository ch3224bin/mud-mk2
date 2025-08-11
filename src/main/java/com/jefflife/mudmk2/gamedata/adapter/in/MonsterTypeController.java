package com.jefflife.mudmk2.gamedata.adapter.in;

import com.jefflife.mudmk2.gamedata.application.service.provided.CreateMonsterTypeUseCase;
import com.jefflife.mudmk2.gamedata.application.service.provided.DeleteMonsterTypeUseCase;
import com.jefflife.mudmk2.gamedata.application.service.provided.GetMonsterTypeUseCase;
import com.jefflife.mudmk2.gamedata.application.service.provided.UpdateMonsterTypeUseCase;
import com.jefflife.mudmk2.gamedata.application.service.model.request.CreateMonsterTypeRequest;
import com.jefflife.mudmk2.gamedata.application.service.model.request.UpdateMonsterTypeRequest;
import com.jefflife.mudmk2.gamedata.application.service.model.response.MonsterTypeResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping(MonsterTypeController.BASE_PATH)
public class MonsterTypeController {
    public static final String BASE_PATH = "/api/v1/monster-types";

    private final CreateMonsterTypeUseCase createMonsterTypeUseCase;
    private final GetMonsterTypeUseCase getMonsterTypeUseCase;
    private final UpdateMonsterTypeUseCase updateMonsterTypeUseCase;
    private final DeleteMonsterTypeUseCase deleteMonsterTypeUseCase;

    public MonsterTypeController(
            final CreateMonsterTypeUseCase createMonsterTypeUseCase,
            final GetMonsterTypeUseCase getMonsterTypeUseCase,
            final UpdateMonsterTypeUseCase updateMonsterTypeUseCase,
            final DeleteMonsterTypeUseCase deleteMonsterTypeUseCase
    ) {
        this.createMonsterTypeUseCase = createMonsterTypeUseCase;
        this.getMonsterTypeUseCase = getMonsterTypeUseCase;
        this.updateMonsterTypeUseCase = updateMonsterTypeUseCase;
        this.deleteMonsterTypeUseCase = deleteMonsterTypeUseCase;
    }

    @PostMapping
    public ResponseEntity<MonsterTypeResponse> createMonsterType(
            @RequestBody final CreateMonsterTypeRequest createMonsterTypeRequest
    ) {
        final MonsterTypeResponse monsterTypeResponse = createMonsterTypeUseCase.createMonsterType(createMonsterTypeRequest);
        return ResponseEntity
                .created(URI.create(String.format("%s/%s", BASE_PATH, monsterTypeResponse.id())))
                .body(monsterTypeResponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MonsterTypeResponse> getMonsterType(@PathVariable final Long id) {
        try {
            final MonsterTypeResponse monsterTypeResponse = getMonsterTypeUseCase.getMonsterType(id);
            return ResponseEntity.ok(monsterTypeResponse);
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<MonsterTypeResponse>> getAllMonsterTypes() {
        final List<MonsterTypeResponse> monsterTypeResponses = getMonsterTypeUseCase.getAllMonsterTypes();
        return ResponseEntity.ok(monsterTypeResponses);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MonsterTypeResponse> updateMonsterType(
            @PathVariable final Long id,
            @RequestBody final UpdateMonsterTypeRequest updateMonsterTypeRequest
    ) {
        try {
            final MonsterTypeResponse monsterTypeResponse = updateMonsterTypeUseCase.updateMonsterType(id, updateMonsterTypeRequest);
            return ResponseEntity.ok(monsterTypeResponse);
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMonsterType(@PathVariable final Long id) {
        try {
            deleteMonsterTypeUseCase.deleteMonsterType(id);
            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
