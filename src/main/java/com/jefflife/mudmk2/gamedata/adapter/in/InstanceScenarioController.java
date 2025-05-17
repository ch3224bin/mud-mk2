package com.jefflife.mudmk2.gamedata.adapter.in;

import com.jefflife.mudmk2.gamedata.application.port.in.InstanceScenarioUseCase;
import com.jefflife.mudmk2.gamedata.application.service.model.request.CreateInstanceScenarioRequest;
import com.jefflife.mudmk2.gamedata.application.service.model.request.UpdateInstanceScenarioRequest;
import com.jefflife.mudmk2.gamedata.application.service.model.response.InstanceScenarioResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
        return ResponseEntity.status(HttpStatus.CREATED).body(createUseCase.createInstanceScenario(request));
    }

    @GetMapping
    public ResponseEntity<List<InstanceScenarioResponse>> getAllInstanceScenarios() {
        return ResponseEntity.ok(getUseCase.getAllInstanceScenarios());
    }

    @GetMapping("/{id}")
    public ResponseEntity<InstanceScenarioResponse> getInstanceScenario(@PathVariable long id) {
        return ResponseEntity.ok(getUseCase.getInstanceScenario(id));
    }

    @GetMapping("/title/{title}")
    public ResponseEntity<InstanceScenarioResponse> getInstanceScenarioByTitle(@PathVariable String title) {
        return ResponseEntity.ok(getUseCase.getInstanceScenarioByTitle(title));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<InstanceScenarioResponse> updateInstanceScenario(
            @PathVariable long id,
            @RequestBody UpdateInstanceScenarioRequest request) {
        return ResponseEntity.ok(updateUseCase.updateInstanceScenario(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInstanceScenario(@PathVariable long id) {
        deleteUseCase.deleteInstanceScenario(id);
        return ResponseEntity.noContent().build();
    }
}
