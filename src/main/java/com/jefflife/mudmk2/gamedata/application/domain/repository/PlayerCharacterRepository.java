package com.jefflife.mudmk2.gamedata.application.domain.repository;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.PlayerCharacter;
import org.springframework.data.repository.CrudRepository;

public interface PlayerCharacterRepository extends CrudRepository<PlayerCharacter, Long> {
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
}