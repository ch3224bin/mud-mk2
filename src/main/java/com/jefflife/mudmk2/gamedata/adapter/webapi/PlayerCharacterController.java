package com.jefflife.mudmk2.gamedata.adapter.webapi;

import com.jefflife.mudmk2.gamedata.adapter.webapi.response.PlayerCharacterSearchResponse;
import com.jefflife.mudmk2.gamedata.application.service.required.PlayerCharacterRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/player-characters")
public class PlayerCharacterController {

    private final PlayerCharacterRepository playerCharacterRepository;

    public PlayerCharacterController(final PlayerCharacterRepository playerCharacterRepository) {
        this.playerCharacterRepository = playerCharacterRepository;
    }

    @GetMapping("/search")
    public ResponseEntity<List<PlayerCharacterSearchResponse>> searchPlayerCharacters(
            @RequestParam(defaultValue = "") final String nickname
    ) {
        final List<PlayerCharacterSearchResponse> result = playerCharacterRepository
            .findByNicknameContaining(nickname)
            .stream()
            .map(PlayerCharacterSearchResponse::from)
            .toList();
        return ResponseEntity.ok(result);
    }
}
