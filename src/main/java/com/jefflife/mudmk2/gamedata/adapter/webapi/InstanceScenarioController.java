package com.jefflife.mudmk2.gamedata.adapter.webapi;

import com.jefflife.mudmk2.gamedata.application.service.provided.InstanceScenarioUseCase;
import com.jefflife.mudmk2.gamedata.application.service.model.request.CreateInstanceScenarioRequest;
import com.jefflife.mudmk2.gamedata.application.service.model.request.UpdateInstanceScenarioRequest;
import com.jefflife.mudmk2.gamedata.adapter.webapi.response.InstanceScenarioResponse;
import com.jefflife.mudmk2.gamedata.application.domain.model.instance.InstanceScenario;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/instance-scenarios")
public class InstanceScenarioController {

    private final InstanceScenarioUseCase.Create createUseCase;
    private final InstanceScenarioUseCase.Update updateUseCase;
    private final InstanceScenarioUseCase.Get getUseCase;
    private final InstanceScenarioUseCase.Delete deleteUseCase;

    @PostMapping
    public ResponseEntity<InstanceScenarioResponse> createInstanceScenario(@RequestBody CreateInstanceScenarioRequest request) {
        InstanceScenario instanceScenario = createUseCase.createInstanceScenario(request);
        InstanceScenarioResponse response = InstanceScenarioResponse.of(instanceScenario);
        return ResponseEntity
                .created(java.net.URI.create("/api/v1/instance-scenarios/" + response.id()))
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<InstanceScenarioResponse>> getAllInstanceScenarios() {
        List<InstanceScenario> scenarios = getUseCase.getAllInstanceScenarios();
        List<InstanceScenarioResponse> responses = scenarios.stream()
                .map(InstanceScenarioResponse::of)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<InstanceScenarioResponse> getInstanceScenario(@PathVariable long id) {
        InstanceScenario instanceScenario = getUseCase.getInstanceScenario(id);
        return ResponseEntity.ok(InstanceScenarioResponse.of(instanceScenario));
    }

    @GetMapping("/title/{title}")
    public ResponseEntity<InstanceScenarioResponse> getInstanceScenarioByTitle(@PathVariable String title) {
        InstanceScenario instanceScenario = getUseCase.getInstanceScenarioByTitle(title);
        return ResponseEntity.ok(InstanceScenarioResponse.of(instanceScenario));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<InstanceScenarioResponse> updateInstanceScenario(
            @PathVariable long id,
            @RequestBody UpdateInstanceScenarioRequest request) {
        InstanceScenario instanceScenario = updateUseCase.updateInstanceScenario(id, request);
        return ResponseEntity.ok(InstanceScenarioResponse.of(instanceScenario));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInstanceScenario(@PathVariable long id) {
        deleteUseCase.deleteInstanceScenario(id);
        return ResponseEntity.noContent().build();
    }
}
