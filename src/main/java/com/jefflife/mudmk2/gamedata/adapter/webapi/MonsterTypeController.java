package com.jefflife.mudmk2.gamedata.adapter.webapi;

import com.jefflife.mudmk2.gamedata.application.service.provided.MonsterTypeCreator;
import com.jefflife.mudmk2.gamedata.application.service.provided.MonsterTypeRemover;
import com.jefflife.mudmk2.gamedata.application.service.provided.MonsterTypeFinder;
import com.jefflife.mudmk2.gamedata.application.service.provided.MonsterTypeModifier;
import com.jefflife.mudmk2.gamedata.application.service.model.request.CreateMonsterTypeRequest;
import com.jefflife.mudmk2.gamedata.application.service.model.request.UpdateMonsterTypeRequest;
import com.jefflife.mudmk2.gamedata.adapter.webapi.response.MonsterTypeResponse;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.MonsterType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping(MonsterTypeController.BASE_PATH)
public class MonsterTypeController {
    public static final String BASE_PATH = "/api/v1/monster-types";

    private final MonsterTypeCreator monsterTypeCreator;
    private final MonsterTypeFinder monsterTypeFinder;
    private final MonsterTypeModifier monsterTypeModifier;
    private final MonsterTypeRemover monsterTypeRemover;

    public MonsterTypeController(
            final MonsterTypeCreator monsterTypeCreator,
            final MonsterTypeFinder monsterTypeFinder,
            final MonsterTypeModifier monsterTypeModifier,
            final MonsterTypeRemover monsterTypeRemover
    ) {
        this.monsterTypeCreator = monsterTypeCreator;
        this.monsterTypeFinder = monsterTypeFinder;
        this.monsterTypeModifier = monsterTypeModifier;
        this.monsterTypeRemover = monsterTypeRemover;
    }

    @PostMapping
    public ResponseEntity<MonsterTypeResponse> createMonsterType(
            @RequestBody final CreateMonsterTypeRequest createMonsterTypeRequest
    ) {
        final MonsterType monsterType = monsterTypeCreator.createMonsterType(createMonsterTypeRequest);
        final MonsterTypeResponse monsterTypeResponse = MonsterTypeResponse.from(monsterType);
        return ResponseEntity
                .created(URI.create(String.format("%s/%s", BASE_PATH, monsterTypeResponse.id())))
                .body(monsterTypeResponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MonsterTypeResponse> getMonsterType(@PathVariable final Long id) {
        try {
            final MonsterType monsterType = monsterTypeFinder.getMonsterType(id);
            final MonsterTypeResponse monsterTypeResponse = MonsterTypeResponse.from(monsterType);
            return ResponseEntity.ok(monsterTypeResponse);
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<MonsterTypeResponse>> getAllMonsterTypes() {
        final List<MonsterType> monsterTypes = monsterTypeFinder.getAllMonsterTypes();
        final List<MonsterTypeResponse> monsterTypeResponses = monsterTypes.stream()
                .map(MonsterTypeResponse::from)
                .toList();
        return ResponseEntity.ok(monsterTypeResponses);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MonsterTypeResponse> updateMonsterType(
            @PathVariable final Long id,
            @RequestBody final UpdateMonsterTypeRequest updateMonsterTypeRequest
    ) {
        try {
            final MonsterType monsterType = monsterTypeModifier.updateMonsterType(id, updateMonsterTypeRequest);
            final MonsterTypeResponse monsterTypeResponse = MonsterTypeResponse.from(monsterType);
            return ResponseEntity.ok(monsterTypeResponse);
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMonsterType(@PathVariable final Long id) {
        try {
            monsterTypeRemover.deleteMonsterType(id);
            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
