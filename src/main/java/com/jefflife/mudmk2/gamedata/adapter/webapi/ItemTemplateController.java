package com.jefflife.mudmk2.gamedata.adapter.webapi;

import com.jefflife.mudmk2.gamedata.application.domain.model.item.ItemTemplate;
import com.jefflife.mudmk2.gamedata.application.domain.model.item.ItemType;
import com.jefflife.mudmk2.gamedata.application.service.model.request.ItemTemplateRequest;
import com.jefflife.mudmk2.gamedata.application.service.provided.*;
import com.jefflife.mudmk2.gamedata.adapter.webapi.response.ItemTemplateResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping(ItemTemplateController.BASE_PATH)
public class ItemTemplateController {
    public static final String BASE_PATH = "/api/v1/item-templates";

    private final ItemTemplateCreator itemTemplateCreator;
    private final ItemTemplateFinder itemTemplateFinder;
    private final ItemTemplateModifier itemTemplateModifier;
    private final ItemTemplateRemover itemTemplateRemover;

    public ItemTemplateController(
            final ItemTemplateCreator itemTemplateCreator,
            final ItemTemplateFinder itemTemplateFinder,
            final ItemTemplateModifier itemTemplateModifier,
            final ItemTemplateRemover itemTemplateRemover
    ) {
        this.itemTemplateCreator = itemTemplateCreator;
        this.itemTemplateFinder = itemTemplateFinder;
        this.itemTemplateModifier = itemTemplateModifier;
        this.itemTemplateRemover = itemTemplateRemover;
    }

    @PostMapping
    public ResponseEntity<ItemTemplateResponse> createItemTemplate(
            @RequestBody final ItemTemplateRequest request
    ) {
        final ItemTemplate template = itemTemplateCreator.create(request);
        final ItemTemplateResponse response = ItemTemplateResponse.from(template);
        return ResponseEntity
            .created(URI.create(String.format("%s/%s", BASE_PATH, response.id())))
            .body(response);
    }

    @GetMapping
    public ResponseEntity<List<ItemTemplateResponse>> getAllItemTemplates(
            @RequestParam(required = false) final ItemType type,
            @RequestParam(required = false) final String name
    ) {
        final List<ItemTemplate> templates;
        if (name != null && !name.isBlank()) {
            templates = itemTemplateFinder.findByNameContaining(name);
        } else if (type != null) {
            templates = itemTemplateFinder.findByType(type);
        } else {
            templates = itemTemplateFinder.findAll();
        }
        return ResponseEntity.ok(templates.stream().map(ItemTemplateResponse::from).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItemTemplateResponse> getItemTemplate(@PathVariable final Long id) {
        try {
            return ResponseEntity.ok(ItemTemplateResponse.from(itemTemplateFinder.findById(id)));
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ItemTemplateResponse> updateItemTemplate(
            @PathVariable final Long id,
            @RequestBody final ItemTemplateRequest request
    ) {
        try {
            return ResponseEntity.ok(ItemTemplateResponse.from(itemTemplateModifier.update(id, request)));
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItemTemplate(@PathVariable final Long id) {
        try {
            itemTemplateRemover.delete(id);
            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
