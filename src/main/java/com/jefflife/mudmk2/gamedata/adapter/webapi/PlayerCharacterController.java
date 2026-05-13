package com.jefflife.mudmk2.gamedata.adapter.webapi;

import com.jefflife.mudmk2.gamedata.adapter.webapi.response.PlayerCharacterSearchResponse;
import com.jefflife.mudmk2.gamedata.application.service.provided.PlayerCharacterFinder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(PlayerCharacterController.BASE_PATH)
public class PlayerCharacterController {

    public static final String BASE_PATH = "/api/v1/player-characters";

    private final PlayerCharacterFinder playerCharacterFinder;

    public PlayerCharacterController(final PlayerCharacterFinder playerCharacterFinder) {
        this.playerCharacterFinder = playerCharacterFinder;
    }

    @GetMapping("/search")
    public ResponseEntity<List<PlayerCharacterSearchResponse>> searchPlayerCharacters(
            @RequestParam(defaultValue = "") final String nickname
    ) {
        final List<PlayerCharacterSearchResponse> result = playerCharacterFinder
            .findByNicknameContaining(nickname)
            .stream()
            .map(PlayerCharacterSearchResponse::from)
            .toList();
        return ResponseEntity.ok(result);
    }
}
