package com.jefflife.mudmk2.gamedata.application.service.required;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.PlayerCharacter;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface PlayerCharacterRepository extends CrudRepository<PlayerCharacter, UUID> {
    /**
     * Find a player character by user ID
     * @param userId the user ID
     * @return the player character, or null if not found
     */
    PlayerCharacter findByUserId(Long userId);

    /**
     * Check if a player character exists for the given user ID
     * @param userId the user ID
     * @return true if a player character exists, false otherwise
     */
    boolean existsByUserId(Long userId);

    /**
     * Check if a player character exists with the given nickname
     * @param nickname the character nickname
     * @return true if a player character exists with the nickname, false otherwise
     */
    boolean existsByNickname(String nickname);
}
