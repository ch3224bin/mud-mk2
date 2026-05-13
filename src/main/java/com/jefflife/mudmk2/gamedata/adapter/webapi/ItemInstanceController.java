package com.jefflife.mudmk2.gamedata.adapter.webapi;

import com.jefflife.mudmk2.gamedata.application.domain.model.item.ItemInstance;
import com.jefflife.mudmk2.gamedata.application.service.model.request.ItemInstancePlaceRequest;
import com.jefflife.mudmk2.gamedata.application.service.provided.ItemInstancePlacer;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/v1/item-instances")
public class ItemInstanceController {

    private final ItemInstancePlacer itemInstancePlacer;

    public ItemInstanceController(final ItemInstancePlacer itemInstancePlacer) {
        this.itemInstancePlacer = itemInstancePlacer;
    }

    @PostMapping("/place")
    public ResponseEntity<Map<String, Object>> placeItemInstance(
            @RequestBody final ItemInstancePlaceRequest request
    ) {
        try {
            final ItemInstance instance = itemInstancePlacer.place(request);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "instanceId", instance.getId()
            ));
        } catch (NoSuchElementException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
}
