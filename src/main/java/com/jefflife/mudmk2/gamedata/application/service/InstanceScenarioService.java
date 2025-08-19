package com.jefflife.mudmk2.gamedata.application.service;

import com.jefflife.mudmk2.gamedata.application.domain.model.instance.InstanceScenario;
import com.jefflife.mudmk2.gamedata.application.service.provided.InstanceScenarioUseCase;
import com.jefflife.mudmk2.gamedata.application.service.required.InstanceScenarioRepository;
import com.jefflife.mudmk2.gamedata.application.service.model.request.CreateInstanceScenarioRequest;
import com.jefflife.mudmk2.gamedata.application.service.model.request.UpdateInstanceScenarioRequest;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
    public InstanceScenario createInstanceScenario(CreateInstanceScenarioRequest request) {
        InstanceScenario instanceScenario = request.toDomain();
        return instanceScenarioRepository.save(instanceScenario);
    }

    @Override
    public InstanceScenario updateInstanceScenario(long id, UpdateInstanceScenarioRequest request) {
        InstanceScenario instanceScenario = instanceScenarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("인스턴스 시나리오를 찾을 수 없습니다. ID: " + id));

        instanceScenario.update(
                request.title(),
                request.description(),
                request.areaId(),
                request.entranceRoomId()
        );

        return instanceScenarioRepository.save(instanceScenario);
    }

    @Override
    @Transactional(readOnly = true)
    public InstanceScenario getInstanceScenario(long id) {
        return instanceScenarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("인스턴스 시나리오를 찾을 수 없습니다. ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<InstanceScenario> getAllInstanceScenarios() {
        return instanceScenarioRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public InstanceScenario getInstanceScenarioByTitle(String title) {
        return instanceScenarioRepository.findByTitle(title)
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
