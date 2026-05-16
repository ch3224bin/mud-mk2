package com.jefflife.mudmk2.gamedata.adapter.webapi;

import com.jefflife.mudmk2.gamedata.adapter.webapi.response.ExternalArtTemplateResponse;
import com.jefflife.mudmk2.gamedata.application.domain.model.item.WeaponType;
import com.jefflife.mudmk2.gamedata.application.domain.model.martialart.ExternalArtTemplate;
import com.jefflife.mudmk2.gamedata.application.service.exception.MartialArtTemplateInUseException;
import com.jefflife.mudmk2.gamedata.application.service.model.request.ExternalArtTemplateRequest;
import com.jefflife.mudmk2.gamedata.application.service.provided.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping(ExternalArtTemplateController.BASE_PATH)
public class ExternalArtTemplateController {
    public static final String BASE_PATH = "/api/v1/external-art-templates";

    private final ExternalArtTemplateCreator creator;
    private final ExternalArtTemplateFinder finder;
    private final ExternalArtTemplateModifier modifier;
    private final ExternalArtTemplateRemover remover;

    public ExternalArtTemplateController(ExternalArtTemplateCreator creator,
                                         ExternalArtTemplateFinder finder,
                                         ExternalArtTemplateModifier modifier,
                                         ExternalArtTemplateRemover remover) {
        this.creator = creator;
        this.finder = finder;
        this.modifier = modifier;
        this.remover = remover;
    }

    @PostMapping
    public ResponseEntity<ExternalArtTemplateResponse> create(@RequestBody ExternalArtTemplateRequest request) {
        try {
            ExternalArtTemplate t = creator.create(request);
            ExternalArtTemplateResponse resp = ExternalArtTemplateResponse.from(t);
            return ResponseEntity.created(URI.create(BASE_PATH + "/" + resp.id())).body(resp);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<ExternalArtTemplateResponse>> list(
            @RequestParam(required = false) WeaponType weaponType,
            @RequestParam(required = false) String name) {
        List<ExternalArtTemplate> templates;
        if (name != null && !name.isBlank()) {
            templates = finder.findByNameContaining(name);
        } else if (weaponType != null) {
            templates = finder.findByWeaponType(weaponType);
        } else {
            templates = finder.findAll();
        }
        return ResponseEntity.ok(templates.stream().map(ExternalArtTemplateResponse::from).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExternalArtTemplateResponse> get(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(ExternalArtTemplateResponse.from(finder.findById(id)));
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExternalArtTemplateResponse> update(@PathVariable Long id,
            @RequestBody ExternalArtTemplateRequest request) {
        try {
            return ResponseEntity.ok(ExternalArtTemplateResponse.from(modifier.update(id, request)));
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
