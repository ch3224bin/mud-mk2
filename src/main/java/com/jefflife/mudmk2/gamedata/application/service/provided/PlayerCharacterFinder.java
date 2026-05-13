package com.jefflife.mudmk2.gamedata.application.service.provided;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.PlayerCharacter;
import java.util.List;

public interface PlayerCharacterFinder {
    List<PlayerCharacter> findByNicknameContaining(String nickname);
}
