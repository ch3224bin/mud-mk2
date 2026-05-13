package com.jefflife.mudmk2.gameplay.application.service;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.Monster;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.MonsterSpawnRoom;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.MonsterType;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.NonPlayerCharacter;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.PlayerCharacter;
import com.jefflife.mudmk2.gamedata.application.service.required.MonsterTypeRepository;
import com.jefflife.mudmk2.gamedata.application.service.required.NonPlayerCharacterRepository;
import com.jefflife.mudmk2.gamedata.application.service.required.PartyRepository;
import com.jefflife.mudmk2.gamedata.application.service.required.PlayerCharacterRepository;
import com.jefflife.mudmk2.gamedata.application.event.PlayerCharacterCreatedEvent;
import com.jefflife.mudmk2.gameplay.application.service.sync.BatchSyncable;
import org.slf4j.Logger;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
public class PersistenceManager {
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(PersistenceManager.class);
    private static final Random random = new Random();

    private final PlayerCharacterRepository playerCharacterRepository;
    private final NonPlayerCharacterRepository nonPlayerCharacterRepository;
    private final MonsterTypeRepository monsterTypeRepository;
    private final GameWorldService gameWorldService;
    private final PartyRepository partyRepository;
    private final List<BatchSyncable> batchSyncables;

    public PersistenceManager(
            final PlayerCharacterRepository playerCharacterRepository,
            final NonPlayerCharacterRepository nonPlayerCharacterRepository,
            final MonsterTypeRepository monsterTypeRepository,
            final GameWorldService gameWorldService,
            final PartyRepository partyRepository,
            final List<BatchSyncable> batchSyncables
    ) {
        this.playerCharacterRepository = playerCharacterRepository;
        this.nonPlayerCharacterRepository = nonPlayerCharacterRepository;
        this.monsterTypeRepository = monsterTypeRepository;
        this.gameWorldService = gameWorldService;
        this.partyRepository = partyRepository;
        this.batchSyncables = batchSyncables;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional(readOnly = true)
    public void loadGameState() {
        // 서버 시작시 DB에서 메모리로 로드
        Iterable<PlayerCharacter> players = playerCharacterRepository.findAll();
        gameWorldService.loadPlayers(players);

        Iterable<NonPlayerCharacter> nonPlayerCharacters = nonPlayerCharacterRepository.findAll();
        gameWorldService.loadNpcs(nonPlayerCharacters);

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

    /**
     * 플레이어 캐릭터 생성 이벤트 처리
     * 새로 생성된 캐릭터를 GameWorldService의 인메모리 캐시에 추가합니다.
     */
    @EventListener
    public void handlePlayerCharacterCreatedEvent(PlayerCharacterCreatedEvent event) {
        final PlayerCharacter playerCharacter = event.getPlayerCharacter();
        playerCharacter.initializeAssociatedEntities(); // 연관 객체 초기화
        gameWorldService.addPlayer(playerCharacter);
        logger.info("New player character added to game world: {}", playerCharacter.getNickname());
    }

    @Transactional
    @Scheduled(fixedDelay = 60_000) // 1분마다 저장
    public void persistGameState() {
        batchSyncables.forEach(BatchSyncable::syncToDb);
    }

    /**
     * 몬스터 리스폰 처리
     */
    @Scheduled(fixedDelay = 5_000) // 5초마다 실행
    public void checkMonsterRespawn() {
        int respawnCount = gameWorldService.respawnMonsters();
        if (respawnCount > 0) {
            logger.debug("Respawned {} monsters", respawnCount);
        }
    }
}
