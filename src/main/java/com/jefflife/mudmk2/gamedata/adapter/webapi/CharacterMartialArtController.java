package com.jefflife.mudmk2.gamedata.adapter.webapi;

import com.jefflife.mudmk2.gamedata.adapter.webapi.response.CharacterMartialArtResponse;
import com.jefflife.mudmk2.gamedata.application.domain.model.martialart.MentalMethodKind;
import com.jefflife.mudmk2.gamedata.application.service.exception.AlreadyLearnedException;
import com.jefflife.mudmk2.gamedata.application.domain.model.martialart.MartialArtSlotFullException;
import com.jefflife.mudmk2.gamedata.application.service.exception.NotLearnedException;
import com.jefflife.mudmk2.gamedata.application.service.provided.ExternalArtTemplateFinder;
import com.jefflife.mudmk2.gamedata.application.service.provided.LearnedMartialArtFinder;
import com.jefflife.mudmk2.gamedata.application.service.provided.MartialArtEquipper;
import com.jefflife.mudmk2.gamedata.application.service.provided.MartialArtLearner;
import com.jefflife.mudmk2.gamedata.application.service.provided.MentalMethodTemplateFinder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.NoSuchElementException;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/player-characters/{pcId}/martial-arts")
public class CharacterMartialArtController {

    private final MartialArtLearner learner;
    private final MartialArtEquipper equipper;
    private final LearnedMartialArtFinder finder;
    private final MentalMethodTemplateFinder mentalFinder;
    private final ExternalArtTemplateFinder externalFinder;

    public CharacterMartialArtController(MartialArtLearner learner,
                                         MartialArtEquipper equipper,
                                         LearnedMartialArtFinder finder,
                                         MentalMethodTemplateFinder mentalFinder,
                                         ExternalArtTemplateFinder externalFinder) {
        this.learner = learner;
        this.equipper = equipper;
        this.finder = finder;
        this.mentalFinder = mentalFinder;
        this.externalFinder = externalFinder;
    }

    public record LearnRequest(Long templateId) {}
    public record EquipRequest(UUID learnedId) {}

    @GetMapping
    public ResponseEntity<CharacterMartialArtResponse> status(@PathVariable UUID pcId) {
        try {
            return ResponseEntity.ok(CharacterMartialArtResponse.from(
                    finder.findByCharacter(pcId),
                    id -> mentalFinder.findById(id),
                    id -> externalFinder.findById(id)));
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/mental-methods/learn")
    public ResponseEntity<Void> learnMental(@PathVariable UUID pcId, @RequestBody LearnRequest req) {
        try {
            learner.learnMentalMethod(pcId, req.templateId());
            return ResponseEntity.status(201).build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (AlreadyLearnedException e) {
            return ResponseEntity.status(409).build();
        }
    }

    @PostMapping("/external-arts/learn")
    public ResponseEntity<Void> learnExternal(@PathVariable UUID pcId, @RequestBody LearnRequest req) {
        try {
            learner.learnExternalArt(pcId, req.templateId());
            return ResponseEntity.status(201).build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (AlreadyLearnedException e) {
            return ResponseEntity.status(409).build();
        }
    }

    @PostMapping("/mental-methods/equip")
    public ResponseEntity<Void> equipMental(@PathVariable UUID pcId, @RequestBody EquipRequest req) {
        try {
            equipper.equipMentalMethod(pcId, req.learnedId());
            return ResponseEntity.ok().build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (NotLearnedException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/external-arts/equip")
    public ResponseEntity<Void> equipExternal(@PathVariable UUID pcId, @RequestBody EquipRequest req) {
        try {
            equipper.equipExternalArt(pcId, req.learnedId());
            return ResponseEntity.ok().build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (NotLearnedException e) {
            return ResponseEntity.badRequest().build();
        } catch (MartialArtSlotFullException e) {
            return ResponseEntity.status(409).build();
        }
    }

    @DeleteMapping("/mental-methods/{kind}")
    public ResponseEntity<Void> unequipMental(@PathVariable UUID pcId, @PathVariable MentalMethodKind kind) {
        try {
            equipper.unequipMentalMethod(pcId, kind);
            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/external-arts/{learnedId}")
    public ResponseEntity<Void> unequipExternal(@PathVariable UUID pcId, @PathVariable UUID learnedId) {
        try {
            equipper.unequipExternalArt(pcId, learnedId);
            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
