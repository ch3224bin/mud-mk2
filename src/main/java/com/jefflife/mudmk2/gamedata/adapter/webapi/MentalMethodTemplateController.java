package com.jefflife.mudmk2.gamedata.adapter.webapi;

import com.jefflife.mudmk2.gamedata.adapter.webapi.response.MentalMethodTemplateResponse;
import com.jefflife.mudmk2.gamedata.application.domain.model.martialart.MentalMethodKind;
import com.jefflife.mudmk2.gamedata.application.domain.model.martialart.MentalMethodTemplate;
import com.jefflife.mudmk2.gamedata.application.service.exception.MartialArtTemplateInUseException;
import com.jefflife.mudmk2.gamedata.application.service.model.request.MentalMethodTemplateRequest;
import com.jefflife.mudmk2.gamedata.application.service.provided.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping(MentalMethodTemplateController.BASE_PATH)
public class MentalMethodTemplateController {
    public static final String BASE_PATH = "/api/v1/mental-method-templates";

    private final MentalMethodTemplateCreator creator;
    private final MentalMethodTemplateFinder finder;
    private final MentalMethodTemplateModifier modifier;
    private final MentalMethodTemplateRemover remover;

    public MentalMethodTemplateController(MentalMethodTemplateCreator creator,
                                          MentalMethodTemplateFinder finder,
                                          MentalMethodTemplateModifier modifier,
                                          MentalMethodTemplateRemover remover) {
        this.creator = creator;
        this.finder = finder;
        this.modifier = modifier;
        this.remover = remover;
    }

    @PostMapping
    public ResponseEntity<MentalMethodTemplateResponse> create(
            @RequestBody MentalMethodTemplateRequest request) {
        try {
            MentalMethodTemplate t = creator.create(request);
            MentalMethodTemplateResponse resp = MentalMethodTemplateResponse.from(t);
            return ResponseEntity.created(URI.create(BASE_PATH + "/" + resp.id())).body(resp);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<MentalMethodTemplateResponse>> list(
            @RequestParam(required = false) MentalMethodKind kind,
            @RequestParam(required = false) String name) {
        List<MentalMethodTemplate> templates;
        if (name != null && !name.isBlank()) {
            templates = finder.findByNameContaining(name);
        } else if (kind != null) {
            templates = finder.findByKind(kind);
        } else {
            templates = finder.findAll();
        }
        return ResponseEntity.ok(templates.stream().map(MentalMethodTemplateResponse::from).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MentalMethodTemplateResponse> get(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(MentalMethodTemplateResponse.from(finder.findById(id)));
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<MentalMethodTemplateResponse> update(@PathVariable Long id,
            @RequestBody MentalMethodTemplateRequest request) {
        try {
            return ResponseEntity.ok(MentalMethodTemplateResponse.from(modifier.update(id, request)));
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        try {
            remover.delete(id);
            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (MartialArtTemplateInUseException e) {
            return ResponseEntity.status(409).build();
        }
    }
}
