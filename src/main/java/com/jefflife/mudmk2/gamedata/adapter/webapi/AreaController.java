package com.jefflife.mudmk2.gamedata.adapter.webapi;

import com.jefflife.mudmk2.gamedata.application.domain.model.map.Area;
import com.jefflife.mudmk2.gamedata.application.service.provided.AreaCreator;
import com.jefflife.mudmk2.gamedata.application.service.provided.AreaRemover;
import com.jefflife.mudmk2.gamedata.application.service.provided.AreaFinder;
import com.jefflife.mudmk2.gamedata.application.service.provided.AreaModifier;
import com.jefflife.mudmk2.gamedata.application.domain.model.map.AreaCreateRequest;
import com.jefflife.mudmk2.gamedata.application.service.model.request.UpdateAreaRequest;
import com.jefflife.mudmk2.gamedata.adapter.webapi.response.AreaResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping(AreaController.BASE_PATH)
public class AreaController {
    public static final String BASE_PATH = "/api/v1/areas";

    private final AreaCreator areaCreator;
    private final AreaModifier areaModifier;
    private final AreaFinder areaFinder;
    private final AreaRemover areaRemover;

    public AreaController(
            final AreaCreator areaCreator,
            final AreaModifier areaModifier,
            final AreaFinder areaFinder,
            final AreaRemover areaRemover
    ) {
        this.areaCreator = areaCreator;
        this.areaModifier = areaModifier;
        this.areaFinder = areaFinder;
        this.areaRemover = areaRemover;
    }

    @PostMapping
    public ResponseEntity<AreaResponse> createArea(
            @RequestBody final AreaCreateRequest areaCreateRequest
    ) {
        Area area = areaCreator.createArea(areaCreateRequest);
        AreaResponse areaResponse = AreaResponse.of(area);
        return ResponseEntity
                .created(URI.create(String.format("%s/%s", BASE_PATH, areaResponse.getId())))
                .body(areaResponse);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<AreaResponse> updateArea(
            @PathVariable final Long id,
            @RequestBody final UpdateAreaRequest updateAreaRequest
    ) {
        Area area = areaModifier.updateArea(id, updateAreaRequest);
        AreaResponse areaResponse = AreaResponse.of(area);
        return ResponseEntity.ok(areaResponse);
    }

    @GetMapping
    public ResponseEntity<Iterable<AreaResponse>> getAreas() {
        List<AreaResponse> areas = areaFinder.getAreas()
                .stream()
                .map(AreaResponse::of)
                .toList();
        return ResponseEntity.ok(areas);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AreaResponse> getArea(@PathVariable final Long id) {
        Area area = areaFinder.getArea(id);
        return ResponseEntity.ok(AreaResponse.of(area));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteArea(@PathVariable final Long id) {
        areaRemover.deleteArea(id);
        return ResponseEntity.noContent().build();
    }
}
