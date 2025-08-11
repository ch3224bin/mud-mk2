package com.jefflife.mudmk2.gamedata.application.service;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.MonsterSpawnRoom;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.MonsterType;
import com.jefflife.mudmk2.gamedata.application.service.required.MonsterTypeRepository;
import com.jefflife.mudmk2.gamedata.application.service.provided.CreateMonsterTypeUseCase;
import com.jefflife.mudmk2.gamedata.application.service.provided.DeleteMonsterTypeUseCase;
import com.jefflife.mudmk2.gamedata.application.service.provided.GetMonsterTypeUseCase;
import com.jefflife.mudmk2.gamedata.application.service.provided.UpdateMonsterTypeUseCase;
import com.jefflife.mudmk2.gamedata.application.service.model.request.CreateMonsterTypeRequest;
import com.jefflife.mudmk2.gamedata.application.service.model.request.MonsterSpawnRoomRequest;
import com.jefflife.mudmk2.gamedata.application.service.model.request.UpdateMonsterTypeRequest;
import com.jefflife.mudmk2.gamedata.application.service.model.response.MonsterTypeResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class MonsterTypeService implements CreateMonsterTypeUseCase, GetMonsterTypeUseCase,
                                           UpdateMonsterTypeUseCase, DeleteMonsterTypeUseCase {

    private final MonsterTypeRepository monsterTypeRepository;

    public MonsterTypeService(MonsterTypeRepository monsterTypeRepository) {
        this.monsterTypeRepository = monsterTypeRepository;
    }

    @Override
    @Transactional
    public MonsterTypeResponse createMonsterType(final CreateMonsterTypeRequest request) {
        MonsterType monsterType = request.toDomain();

        // 스폰 룸 정보 추가
        if (request.spawnRooms() != null && !request.spawnRooms().isEmpty()) {
            for (MonsterSpawnRoomRequest spawnRoomRequest : request.spawnRooms()) {
                MonsterSpawnRoom spawnRoom = new MonsterSpawnRoom(
                        spawnRoomRequest.roomId(),
                        monsterType,
                        spawnRoomRequest.spawnCount()
                );
                monsterType.addSpawnRoom(spawnRoom);
            }
        }

        final MonsterType savedMonsterType = monsterTypeRepository.save(monsterType);
        return MonsterTypeResponse.from(savedMonsterType);
    }

    @Override
    @Transactional(readOnly = true)
    public MonsterTypeResponse getMonsterType(Long id) {
        MonsterType monsterType = monsterTypeRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("MonsterType not found with id: " + id));
        return MonsterTypeResponse.from(monsterType);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MonsterTypeResponse> getAllMonsterTypes() {
        return monsterTypeRepository.findAll().stream()
                .map(MonsterTypeResponse::from)
                .toList();
    }

    @Override
    @Transactional
    public MonsterTypeResponse updateMonsterType(Long id, UpdateMonsterTypeRequest request) {
        MonsterType monsterType = monsterTypeRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("MonsterType not found with id: " + id));

        updateMonsterTypeFromRequest(monsterType, request);

        // 스폰 룸 정보 업데이트
        if (request.spawnRooms() != null) {
            List<MonsterSpawnRoom> newSpawnRooms = request.spawnRooms().stream()
                    .map(spawnRoomRequest -> new MonsterSpawnRoom(
                            spawnRoomRequest.roomId(),
                            monsterType,
                            spawnRoomRequest.spawnCount()
                    ))
                    .collect(Collectors.toList());

            monsterType.clearAndAddAll(newSpawnRooms);
        }

        MonsterType updatedMonsterType = monsterTypeRepository.save(monsterType);
        return MonsterTypeResponse.from(updatedMonsterType);
    }

    @Override
    @Transactional
    public void deleteMonsterType(Long id) {
        if (!monsterTypeRepository.existsById(id)) {
            throw new NoSuchElementException("MonsterType not found with id: " + id);
        }
        monsterTypeRepository.deleteById(id);
    }

    private void updateMonsterTypeFromRequest(MonsterType monsterType, UpdateMonsterTypeRequest request) {
        monsterType.setName(request.name());
        monsterType.setDescription(request.description());
        monsterType.setGender(request.gender());
        monsterType.setBaseHp(request.baseHp());
        monsterType.setBaseMp(request.baseMp());
        monsterType.setBaseStr(request.baseStr());
        monsterType.setBaseDex(request.baseDex());
        monsterType.setBaseCon(request.baseCon());
        monsterType.setBaseIntelligence(request.baseIntelligence());
        monsterType.setBasePow(request.basePow());
        monsterType.setBaseCha(request.baseCha());
        monsterType.setBaseExperience(request.baseExperience());
        monsterType.setHpPerLevel(request.hpPerLevel());
        monsterType.setStrPerLevel(request.strPerLevel());
        monsterType.setDexPerLevel(request.dexPerLevel());
        monsterType.setConPerLevel(request.conPerLevel());
        monsterType.setIntelligencePerLevel(request.intelligencePerLevel());
        monsterType.setPowPerLevel(request.powPerLevel());
        monsterType.setChaPerLevel(request.chaPerLevel());
        monsterType.setExpPerLevel(request.expPerLevel());
        monsterType.setAggressiveness(request.aggressiveness());
        monsterType.setRespawnTime(request.respawnTime());
    }
}
