package com.jefflife.mudmk2.gameplay.adapter.in.webapi;

import com.jefflife.mudmk2.gameplay.adapter.in.webapi.dto.SimulationSpawnRequest;
import com.jefflife.mudmk2.gameplay.adapter.in.webapi.dto.SpawnedMonsterResponse;
import com.jefflife.mudmk2.gameplay.application.service.SimulationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/simulation")
public class SimulationController {

    private final SimulationService simulationService;

    public SimulationController(SimulationService simulationService) {
        this.simulationService = simulationService;
    }

    @PostMapping("/spawn")
    public ResponseEntity<SpawnedMonsterResponse> spawn(@RequestBody SimulationSpawnRequest request) {
        return ResponseEntity.ok(simulationService.spawn(request));
    }

    @GetMapping("/room")
    public ResponseEntity<List<SpawnedMonsterResponse>> listSpawned() {
        return ResponseEntity.ok(simulationService.listSpawned());
    }

    @DeleteMapping("/spawn/{id}")
    public ResponseEntity<Void> remove(@PathVariable UUID id) {
        boolean removed = simulationService.remove(id);
        return removed ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
