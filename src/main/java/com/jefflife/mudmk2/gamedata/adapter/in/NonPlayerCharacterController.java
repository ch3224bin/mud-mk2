package com.jefflife.mudmk2.gamedata.adapter.in;

import com.jefflife.mudmk2.gamedata.application.port.in.CreateNonPlayerCharacterUseCase;
import com.jefflife.mudmk2.gamedata.application.service.model.request.CreateNonPlayerCharacterRequest;
import com.jefflife.mudmk2.gamedata.application.service.model.response.NonPlayerCharacterResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping(NonPlayerCharacterController.BASE_PATH)
public class NonPlayerCharacterController {
    public static final String BASE_PATH = "/api/v1/npcs";

    private final CreateNonPlayerCharacterUseCase createNonPlayerCharacterUseCase;

    public NonPlayerCharacterController(
            final CreateNonPlayerCharacterUseCase createNonPlayerCharacterUseCase
    ) {
        this.createNonPlayerCharacterUseCase = createNonPlayerCharacterUseCase;
    }

    @PostMapping
    public ResponseEntity<NonPlayerCharacterResponse> createNonPlayerCharacter(
            @RequestBody final CreateNonPlayerCharacterRequest createNonPlayerCharacterRequest
    ) {
        final NonPlayerCharacterResponse nonPlayerCharacterResponse = createNonPlayerCharacterUseCase.createNonPlayerCharacter(createNonPlayerCharacterRequest);
        return ResponseEntity
                .created(URI.create(String.format("%s/%s", BASE_PATH, nonPlayerCharacterResponse.id())))
                .body(nonPlayerCharacterResponse);
    }
}