package com.jefflife.mudmk2.gamedata.application.service;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.CharacterClass;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.Inventory;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.PlayerCharacter;
import com.jefflife.mudmk2.gamedata.application.service.required.PlayerCharacterRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlayerCharacterServiceTest {

    @Mock
    private PlayerCharacterRepository playerCharacterRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private PlayerCharacterService playerCharacterService;

    @Test
    void createCharacter_shouldCreateWithInventory() {
        ArgumentCaptor<PlayerCharacter> captor = ArgumentCaptor.forClass(PlayerCharacter.class);
        when(playerCharacterRepository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

        playerCharacterService.createCharacter(1L, "TestHero", CharacterClass.WARRIOR);

        PlayerCharacter saved = captor.getValue();
        assertThat(saved.getInventory()).isNotNull();
        assertThat(saved.getInventory().getMaxWeightCapacity())
                .isEqualTo(Inventory.DEFAULT_MAX_WEIGHT_CAPACITY);
    }
}
