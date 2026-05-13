package com.jefflife.mudmk2.gamedata.application.service;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.BaseCharacter;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.CharacterClass;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.CharacterStats;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.Gender;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.Inventory;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.PlayableCharacter;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.PlayerCharacter;
import com.jefflife.mudmk2.gamedata.application.service.provided.PlayerCharacterFinder;
import com.jefflife.mudmk2.gamedata.application.service.required.PlayerCharacterRepository;
import com.jefflife.mudmk2.gamedata.application.event.PlayerCharacterCreatedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class PlayerCharacterService implements PlayerCharacterFinder {
    private final PlayerCharacterRepository playerCharacterRepository;
    private final ApplicationEventPublisher eventPublisher;

    public PlayerCharacterService(
            PlayerCharacterRepository playerCharacterRepository,
            ApplicationEventPublisher eventPublisher
    ) {
        this.playerCharacterRepository = playerCharacterRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public List<PlayerCharacter> findByNicknameContaining(final String nickname) {
        return playerCharacterRepository.findByNicknameContaining(nickname);
    }

    /**
     * Check if a player character exists for the given user ID
     * @param userId the user ID
     * @return true if a player character exists, false otherwise
     */
    public boolean hasCharacter(Long userId) {
        return playerCharacterRepository.existsByUserId(userId);
    }

    /**
     * Get a player character by user ID
     * @param userId the user ID
     * @return the player character, or null if not found
     */
    public PlayerCharacter getCharacterByUserId(Long userId) {
        return playerCharacterRepository.findByUserId(userId);
    }

    /**
     * Create a new player character
     * @param userId the user ID
     * @param name the character name
     * @param characterClass the character class
     * @param gender the character gender
     * @return the created player character
     */
    @Transactional
    public PlayerCharacter createCharacter(Long userId, String name, CharacterClass characterClass, Gender gender) {
        // Create base character with stats based on class
        final BaseCharacter baseCharacter = createBaseCharacter(name, characterClass, gender);

        // Create playable character
        final PlayableCharacter playableCharacter = PlayableCharacter.builder()
                .level(1)
                .experience(0)
                .nextLevelExp(100)
                .conversable(true)
                .build();

        // Create inventory
        final Inventory inventory = Inventory.create(Inventory.DEFAULT_MAX_WEIGHT_CAPACITY);

        // Create player character
        final PlayerCharacter playerCharacter = new PlayerCharacter(
                null, // ID will be generated
                baseCharacter,
                playableCharacter,
                userId,
                name, // Use the same name as nickname
                characterClass,
                true, // Online
                LocalDateTime.now(), // Last active now
                inventory
        );

        // Save character
        final PlayerCharacter savedCharacter = playerCharacterRepository.save(playerCharacter);

        // Publish event
        eventPublisher.publishEvent(new PlayerCharacterCreatedEvent(this, savedCharacter));

        return savedCharacter;
    }

    /**
     * Create a new player character with default gender (MALE)
     * @param userId the user ID
     * @param name the character name
     * @param characterClass the character class
     * @return the created player character
     */
    @Transactional
    public PlayerCharacter createCharacter(Long userId, String name, CharacterClass characterClass) {
        return createCharacter(userId, name, characterClass, Gender.MALE);
    }

    /**
     * Create a base character with stats based on class
     * @param name the character name
     * @param characterClass the character class
     * @param gender the character gender
     * @return the base character
     */
    private BaseCharacter createBaseCharacter(String name, CharacterClass characterClass, Gender gender) {
        int baseAttr = 10;
        int hp = baseAttr * CharacterStats.HP_PER_PHYSIQUE;   // 100
        int mp = baseAttr * CharacterStats.MP_PER_MERIDIAN;   // 50
        int ap = baseAttr * CharacterStats.AP_PER_AGILITY;    // 80

        return BaseCharacter.builder()
                .name(name)
                .background("새로운 강호인")
                .gender(gender)
                .hp(hp).mp(mp).ap(ap)
                .vigor(baseAttr).physique(baseAttr).agility(baseAttr)
                .intellect(baseAttr).will(baseAttr).meridian(baseAttr)
                .innerPower(0).specialTechnique(0).lightStep(0)
                .fistsAndPalms(0).swordMethod(0).bladeMethod(0)
                .longWeapon(0).esotericWeapon(0).archery(0)
                .roomId(1L)
                .alive(true)
                .build();
    }
}
