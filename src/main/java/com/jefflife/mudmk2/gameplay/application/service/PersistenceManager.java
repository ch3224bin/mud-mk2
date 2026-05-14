package com.jefflife.mudmk2.gameplay.application.service;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.Monster;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.MonsterSpawnRoom;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.MonsterType;
import com.jefflife.mudmk2.gamedata.application.service.required.MonsterTypeRepository;
import com.jefflife.mudmk2.gamedata.application.service.required.PartyRepository;
import org.slf4j.Logger;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
public class PersistenceManager {
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(PersistenceManager.class);
    private static final Random random = new Random();

    private final MonsterTypeRepository monsterTypeRepository;
    private final GameWorldService gameWorldService;
    private final PartyRepository partyRepository;

    public PersistenceManager(
            final MonsterTypeRepository monsterTypeRepository,
            final GameWorldService gameWorldService,
            final PartyRepository partyRepository
    ) {
        this.monsterTypeRepository = monsterTypeRepository;
        this.gameWorldService = gameWorldService;
        this.partyRepository = partyRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional(readOnly = true)
    public void loadGameState() {
        gameWorldService.loadParties(partyRepository.findAll());

        // MonsterType을 기반으로 Monster 생성 및 로드
        loadMonsters();

        logger.info("loadGameState finished");
    }

    /**
     * MonsterType을 기반으로 Monster를 생성하고 GameWorldService에 로드합니다.
     */
    private void loadMonsters() {
        List<Monster> monsters = new ArrayList<>();
        Iterable<MonsterType> monsterTypes = monsterTypeRepository.findAll();

        for (MonsterType monsterType : monsterTypes) {
            if (monsterType.getMonsterSpawnRooms() != null && monsterType.getMonsterSpawnRooms().getSpawnRooms() != null) {
                for (MonsterSpawnRoom spawnRoom : monsterType.getMonsterSpawnRooms().getSpawnRooms()) {
                    // 각 스폰 룸에서 지정된 수만큼 몬스터 생성
                    for (int i = 0; i < spawnRoom.getSpawnCount(); i++) {
                        // 몬스터 레벨 무작위 설정 (1~5 사이)
                        int monsterLevel = 1 + random.nextInt(5);
                        Monster monster = Monster.createFromType(monsterType, monsterLevel, spawnRoom.getRoomId());
                        monsters.add(monster);
                    }
                }
            }
        }

        gameWorldService.loadMonsters(monsters);
        logger.info("Created and loaded {} monsters from {} monster types", monsters.size(), monsterTypes.spliterator().estimateSize());
    }

}
