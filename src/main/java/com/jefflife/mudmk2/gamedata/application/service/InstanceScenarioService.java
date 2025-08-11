package com.jefflife.mudmk2.gamedata.application.service;

import com.jefflife.mudmk2.gamedata.application.domain.model.instance.InstanceScenario;
import com.jefflife.mudmk2.gamedata.application.port.in.InstanceScenarioUseCase;
import com.jefflife.mudmk2.gamedata.application.service.required.InstanceScenarioRepository;
import com.jefflife.mudmk2.gamedata.application.service.model.request.CreateInstanceScenarioRequest;
import com.jefflife.mudmk2.gamedata.application.service.model.request.UpdateInstanceScenarioRequest;
import com.jefflife.mudmk2.gamedata.application.service.model.response.InstanceScenarioResponse;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Transactional
@Service
public class InstanceScenarioService implements
        InstanceScenarioUseCase.Create,
        InstanceScenarioUseCase.Update,
        InstanceScenarioUseCase.Get,
        InstanceScenarioUseCase.Delete {

    private final InstanceScenarioRepository instanceScenarioRepository;

    @Override
    public InstanceScenarioResponse createInstanceScenario(CreateInstanceScenarioRequest request) {
        InstanceScenario instanceScenario = request.toDomain();
        InstanceScenario savedScenario = instanceScenarioRepository.save(instanceScenario);
        return InstanceScenarioResponse.of(savedScenario);
    }

    @Override
    public InstanceScenarioResponse updateInstanceScenario(long id, UpdateInstanceScenarioRequest request) {
        InstanceScenario instanceScenario = instanceScenarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("인스턴스 시나리오를 찾을 수 없습니다. ID: " + id));

        instanceScenario.update(
                request.title(),
                request.description(),
                request.areaId(),
                request.entranceRoomId()
        );

        return InstanceScenarioResponse.of(instanceScenarioRepository.save(instanceScenario));
    }

    @Override
    @Transactional(readOnly = true)
    public InstanceScenarioResponse getInstanceScenario(long id) {
        return instanceScenarioRepository.findById(id)
                .map(InstanceScenarioResponse::of)
                .orElseThrow(() -> new EntityNotFoundException("인스턴스 시나리오를 찾을 수 없습니다. ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<InstanceScenarioResponse> getAllInstanceScenarios() {
        return instanceScenarioRepository.findAll().stream()
                .map(InstanceScenarioResponse::of)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public InstanceScenarioResponse getInstanceScenarioByTitle(String title) {
        return instanceScenarioRepository.findByTitle(title)
                .map(InstanceScenarioResponse::of)
                .orElseThrow(() -> new EntityNotFoundException("인스턴스 시나리오를 찾을 수 없습니다. 제목: " + title));
    }

    @Override
    public void deleteInstanceScenario(long id) {
        if (!instanceScenarioRepository.existsById(id)) {
            throw new EntityNotFoundException("인스턴스 시나리오를 찾을 수 없습니다. ID: " + id);
        }
        instanceScenarioRepository.deleteById(id);
    }
}
