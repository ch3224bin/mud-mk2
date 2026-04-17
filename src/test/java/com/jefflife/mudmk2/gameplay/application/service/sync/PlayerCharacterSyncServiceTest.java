package com.jefflife.mudmk2.gameplay.application.service.sync;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.PlayerCharacter;
import com.jefflife.mudmk2.gamedata.application.service.required.PlayerCharacterRepository;
import com.jefflife.mudmk2.gameplay.application.service.GameWorldService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlayerCharacterSyncServiceTest {

    @Mock
    private PlayerCharacterRepository playerCharacterRepository;

    @Mock
    private GameWorldService gameWorldService;

    private PlayerCharacterSyncService sut;

    @BeforeEach
    void setUp() {
        sut = new PlayerCharacterSyncService(playerCharacterRepository, gameWorldService);
    }

    @Test
    @DisplayName("syncToDb 호출 시 활성 플레이어 전체를 저장한다")
    void syncToDb_savesAllActivePlayers() {
        PlayerCharacter pc = mock(PlayerCharacter.class);
        when(gameWorldService.getActivePlayers()).thenReturn(List.of(pc));

        sut.syncToDb();

        verify(playerCharacterRepository).saveAll(List.of(pc));
    }
}
