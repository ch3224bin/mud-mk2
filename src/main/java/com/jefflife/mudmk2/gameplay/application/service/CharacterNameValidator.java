package com.jefflife.mudmk2.gameplay.application.service;

import com.jefflife.mudmk2.gamedata.application.service.required.PlayerCharacterRepository;
import org.springframework.stereotype.Component;

/**
 * Validator for character names.
 * Responsible for validating character names according to the rules.
 */
@Component
public class CharacterNameValidator {

    private final PlayerCharacterRepository playerCharacterRepository;

    public CharacterNameValidator(PlayerCharacterRepository playerCharacterRepository) {
        this.playerCharacterRepository = playerCharacterRepository;
    }

    /**
     * Validates a character name.
     * @param name the name to validate
     * @return a ValidationResult indicating success or failure with an error message
     */
    public ValidationResult validate(String name) {
        // Check if the name contains spaces
        if (name.contains(" ")) {
            return ValidationResult.failure("케릭터 이름에는 공백이 포함될 수 없습니다. 다시 입력해주세요");
        }

        // Check if the name contains only Korean or English characters
        if (!name.matches("^[가-힣a-zA-Z]+$")) {
            return ValidationResult.failure("케릭터 이름은 한글 또는 영문으로만 작성해야 합니다. 다시 입력해주세요");
        }

        // Check if the name already exists
        if (playerCharacterRepository.existsByNickname(name)) {
            return ValidationResult.failure("이미 존재하는 이름입니다. 다시 입력해주세요");
        }

        return ValidationResult.success();
    }
}
