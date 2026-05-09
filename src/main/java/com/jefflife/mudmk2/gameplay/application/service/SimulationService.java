package com.jefflife.mudmk2.gameplay.application.service;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.Monster;
import com.jefflife.mudmk2.gameplay.adapter.in.webapi.dto.SimulationSpawnRequest;
import com.jefflife.mudmk2.gameplay.adapter.in.webapi.dto.SpawnedMonsterResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SimulationService {

    private final GameWorldService gameWorldService;
    private final Map<UUID, Monster> spawnedMonsters = new ConcurrentHashMap<>();

    @Value("${simulation.room-id:1}")
    private Long simulationRoomId;

    public SimulationService(GameWorldService gameWorldService) {
        this.gameWorldService = gameWorldService;
    }

    public SpawnedMonsterResponse spawn(SimulationSpawnRequest request) {
        Monster monster = Monster.createSimulation(
            request.getName(),
            request.getVigor(), request.getPhysique(), request.getAgility(),
            request.getIntellect(), request.getWill(), request.getMeridian(),
            request.getInnerPower(), request.getSpecialTechnique(), request.getLightStep(),
            request.getFistsAndPalms(), request.getSwordMethod(), request.getBladeMethod(),
            request.getLongWeapon(), request.getEsotericWeapon(), request.getArchery(),
            request.getWeaponBaseDamage(), request.getEquipmentArmor(), request.getEquipmentArmorPct(),
            simulationRoomId
        );
        spawnedMonsters.put(monster.getId(), monster);
        gameWorldService.addMonster(monster);
        return SpawnedMonsterResponse.from(monster);
    }

    public List<SpawnedMonsterResponse> listSpawned() {
        return spawnedMonsters.values().stream()
            .map(SpawnedMonsterResponse::from)
            .toList();
    }

    public boolean remove(UUID monsterId) {
        Monster removed = spawnedMonsters.remove(monsterId);
        if (removed != null) {
            gameWorldService.removeMonster(monsterId);
            return true;
        }
        return false;
    }
}
