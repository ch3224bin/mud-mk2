package com.jefflife.mudmk2.gameplay.application.service.sync;

import com.jefflife.mudmk2.gamedata.application.service.required.PlayerCharacterRepository;
import com.jefflife.mudmk2.gameplay.application.service.GameWorldService;
import org.springframework.stereotype.Component;

@Component
public class PlayerCharacterSyncService implements BatchSyncable {

    private final PlayerCharacterRepository playerCharacterRepository;
    private final GameWorldService gameWorldService;

    public PlayerCharacterSyncService(
            final PlayerCharacterRepository playerCharacterRepository,
            final GameWorldService gameWorldService
    ) {
        this.playerCharacterRepository = playerCharacterRepository;
        this.gameWorldService = gameWorldService;
    }

    @Override
    public void syncToDb() {
        playerCharacterRepository.saveAll(gameWorldService.getActivePlayers());
    }
}
