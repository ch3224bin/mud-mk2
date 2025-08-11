package com.jefflife.mudmk2.gamedata.application.service;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.BaseCharacter;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.CharacterClass;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.Gender;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.PlayableCharacter;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.PlayerCharacter;
import com.jefflife.mudmk2.gamedata.application.service.required.PlayerCharacterRepository;
import com.jefflife.mudmk2.gamedata.application.event.PlayerCharacterCreatedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class PlayerCharacterService {
    private final PlayerCharacterRepository playerCharacterRepository;
    private final ApplicationEventPublisher eventPublisher;

    public PlayerCharacterService(
            PlayerCharacterRepository playerCharacterRepository,
            ApplicationEventPublisher eventPublisher
    ) {
        this.playerCharacterRepository = playerCharacterRepository;
        this.eventPublisher = eventPublisher;
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

        // Create player character
        final PlayerCharacter playerCharacter = new PlayerCharacter(
                null, // ID will be generated
                baseCharacter,
                playableCharacter,
                userId,
                name, // Use the same name as nickname
                characterClass,
                true, // Online
                LocalDateTime.now() // Last active now
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
        // Set base stats based on class
        int hp, maxHp, mp, maxMp, str, dex, con, intelligence, pow, cha;

        switch (characterClass) {
            case WARRIOR:
                hp = maxHp = 100;
                mp = maxMp = 20;
                str = 15;
                dex = 10;
                con = 15;
                intelligence = 8;
                pow = 5;
                cha = 10;
                break;
            case MAGE:
                hp = maxHp = 60;
                mp = maxMp = 100;
                str = 5;
                dex = 8;
                con = 8;
                intelligence = 15;
                pow = 15;
                cha = 10;
                break;
            case ROGUE:
                hp = maxHp = 70;
                mp = maxMp = 40;
                str = 10;
                dex = 15;
                con = 10;
                intelligence = 10;
                pow = 8;
                cha = 12;
                break;
            case CLERIC:
                hp = maxHp = 80;
                mp = maxMp = 80;
                str = 8;
                dex = 8;
                con = 12;
                intelligence = 12;
                pow = 12;
                cha = 15;
                break;
            case RANGER:
                hp = maxHp = 80;
                mp = maxMp = 50;
                str = 12;
                dex = 15;
                con = 12;
                intelligence = 10;
                pow = 8;
                cha = 8;
                break;
            default:
                hp = maxHp = 80;
                mp = maxMp = 50;
                str = 10;
                dex = 10;
                con = 10;
                intelligence = 10;
                pow = 10;
                cha = 10;
                break;
        }

        return BaseCharacter.builder()
                .name(name)
                .background("A new adventurer")
                .hp(hp)
                .maxHp(maxHp)
                .mp(mp)
                .maxMp(maxMp)
                .str(str)
                .dex(dex)
                .con(con)
                .intelligence(intelligence)
                .pow(pow)
                .cha(cha)
                .roomId(1L) // Start in room 1
                .alive(true)
                .gender(gender)
                .build();
    }
}
