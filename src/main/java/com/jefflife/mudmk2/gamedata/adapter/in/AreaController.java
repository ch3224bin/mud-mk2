package com.jefflife.mudmk2.gamedata.adapter.in;

import com.jefflife.mudmk2.gamedata.application.port.in.CreateAreaUseCase;
import com.jefflife.mudmk2.gamedata.application.port.in.DeleteAreaUseCase;
import com.jefflife.mudmk2.gamedata.application.port.in.GetAreaUseCase;
import com.jefflife.mudmk2.gamedata.application.port.in.UpdateAreaUseCase;
import com.jefflife.mudmk2.gamedata.application.service.model.request.CreateAreaRequest;
import com.jefflife.mudmk2.gamedata.application.service.model.request.UpdateAreaRequest;
import com.jefflife.mudmk2.gamedata.application.service.model.response.AreaResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping(AreaController.BASE_PATH)
public class AreaController {
    public static final String BASE_PATH = "/api/v1/areas";

    private final CreateAreaUseCase createAreaUseCase;
    private final UpdateAreaUseCase updateAreaUseCase;
    private final GetAreaUseCase getAreaUseCase;
    private final DeleteAreaUseCase deleteAreaUseCase;

    public AreaController(
            final CreateAreaUseCase createAreaUseCase,
            final UpdateAreaUseCase updateAreaUseCase,
            final GetAreaUseCase getAreaUseCase,
            final DeleteAreaUseCase deleteAreaUseCase
    ) {
        this.createAreaUseCase = createAreaUseCase;
        this.updateAreaUseCase = updateAreaUseCase;
        this.getAreaUseCase = getAreaUseCase;
        this.deleteAreaUseCase = deleteAreaUseCase;
    }

    @PostMapping
    public ResponseEntity<AreaResponse> createArea(
            @RequestBody final CreateAreaRequest createAreaRequest
    ) {
        AreaResponse areaResponse = createAreaUseCase.createArea(createAreaRequest);
        return ResponseEntity
                .created(URI.create(String.format("%s/%s", BASE_PATH, areaResponse.getId())))
                .body(areaResponse);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<AreaResponse> updateArea(
            @PathVariable final Long id,
            @RequestBody final UpdateAreaRequest updateAreaRequest
    ) {
        AreaResponse areaResponse = updateAreaUseCase.updateArea(id, updateAreaRequest);
        return ResponseEntity.ok(areaResponse);
    }

    @GetMapping
    public ResponseEntity<Iterable<AreaResponse>> getAreas() {
        return ResponseEntity.ok(getAreaUseCase.getAreas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AreaResponse> getArea(@PathVariable final Long id) {
        return ResponseEntity.ok(getAreaUseCase.getArea(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteArea(@PathVariable final Long id) {
        deleteAreaUseCase.deleteArea(id);
        return ResponseEntity.noContent().build();
    }
}
