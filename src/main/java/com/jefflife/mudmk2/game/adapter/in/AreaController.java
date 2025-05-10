package com.jefflife.mudmk2.game.adapter.in;

import com.jefflife.mudmk2.game.application.port.in.CreateAreaUseCase;
import com.jefflife.mudmk2.game.application.port.in.GetAreaUseCase;
import com.jefflife.mudmk2.game.application.port.in.UpdateAreaUseCase;
import com.jefflife.mudmk2.game.application.service.model.request.CreateAreaRequest;
import com.jefflife.mudmk2.game.application.service.model.request.UpdateAreaRequest;
import com.jefflife.mudmk2.game.application.service.model.response.AreaResponse;
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

    public AreaController(
            final CreateAreaUseCase createAreaUseCase,
            final UpdateAreaUseCase updateAreaUseCase,
            final GetAreaUseCase getAreaUseCase
    ) {
        this.createAreaUseCase = createAreaUseCase;
        this.updateAreaUseCase = updateAreaUseCase;
        this.getAreaUseCase = getAreaUseCase;
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

    @GetMapping
    @RequestMapping("/{id}")
    public ResponseEntity<AreaResponse> getArea(@PathVariable final Long id) {
        return ResponseEntity.ok(getAreaUseCase.getArea(id));
    }
}
