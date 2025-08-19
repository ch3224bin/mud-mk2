package com.jefflife.mudmk2.gamedata.application.service.required;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.NonPlayerCharacter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface NonPlayerCharacterRepository extends JpaRepository<NonPlayerCharacter, UUID> {
}
