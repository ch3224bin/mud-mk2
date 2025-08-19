package com.jefflife.mudmk2.gamedata.application.service;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.MonsterSpawnRoom;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.MonsterType;
import com.jefflife.mudmk2.gamedata.application.service.required.MonsterTypeRepository;
import com.jefflife.mudmk2.gamedata.application.service.provided.MonsterTypeCreator;
import com.jefflife.mudmk2.gamedata.application.service.provided.MonsterTypeRemover;
import com.jefflife.mudmk2.gamedata.application.service.provided.MonsterTypeFinder;
import com.jefflife.mudmk2.gamedata.application.service.provided.MonsterTypeModifier;
import com.jefflife.mudmk2.gamedata.application.service.model.request.CreateMonsterTypeRequest;
import com.jefflife.mudmk2.gamedata.application.service.model.request.MonsterSpawnRoomRequest;
import com.jefflife.mudmk2.gamedata.application.service.model.request.UpdateMonsterTypeRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Transactional
@Service
public class MonsterTypeService implements MonsterTypeCreator, MonsterTypeFinder,
        MonsterTypeModifier, MonsterTypeRemover {

    private final MonsterTypeRepository monsterTypeRepository;

    public MonsterTypeService(MonsterTypeRepository monsterTypeRepository) {
        this.monsterTypeRepository = monsterTypeRepository;
    }

    @Override
    public MonsterType createMonsterType(final CreateMonsterTypeRequest request) {
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

        return monsterTypeRepository.save(monsterType);
    }

    @Override
    @Transactional(readOnly = true)
    public MonsterType getMonsterType(Long id) {
        return monsterTypeRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("MonsterType not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MonsterType> getAllMonsterTypes() {
        return monsterTypeRepository.findAll();
    }

    @Override
    public MonsterType updateMonsterType(Long id, UpdateMonsterTypeRequest request) {
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

        return monsterTypeRepository.save(monsterType);
    }

    @Override
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
