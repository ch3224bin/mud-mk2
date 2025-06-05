package com.jefflife.mudmk2.gameplay.application.service;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.CharacterClass;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.Gender;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.PlayerCharacter;
import com.jefflife.mudmk2.gamedata.application.service.PlayerCharacterService;
import com.jefflife.mudmk2.gameplay.application.domain.model.character.CharacterCreationState;
import com.jefflife.mudmk2.gameplay.application.port.out.SendMessageToUserPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for managing character creation.
 */
@Service
public class CharacterCreationService {
    private static final Logger logger = LoggerFactory.getLogger(CharacterCreationService.class);

    private final PlayerCharacterService playerCharacterService;
    private final SendMessageToUserPort sendMessageToUserPort;

    // Store creation states by username
    private final Map<Long, CharacterCreationState> creationStates = new ConcurrentHashMap<>();

    public CharacterCreationService(
            PlayerCharacterService playerCharacterService,
            SendMessageToUserPort sendMessageToUserPort) {
        this.playerCharacterService = playerCharacterService;
        this.sendMessageToUserPort = sendMessageToUserPort;
    }

    /**
     * Start character creation for a user.
     * @param userId the user ID
     */
    public void startCharacterCreation(Long userId) {
        // Check if the user already has a character
        if (playerCharacterService.hasCharacter(userId)) {
            sendMessageToUserPort.messageToUser(userId, "이미 캐릭터가 있습니다.");
            return;
        }

        // Create a new state and store it
        CharacterCreationState state = new CharacterCreationState(userId);
        creationStates.put(userId, state);

        // Move to the first step (asking for name)
        state.nextStep();

        // Send welcome message
        sendMessageToUserPort.messageToUser(userId, "게임에 오신 것을 환영합니다! 캐릭터를 생성해 봅시다.");
        sendMessageToUserPort.messageToUser(userId, "캐릭터의 이름은 무엇인가요?");
    }

    /**
     * Process a message from a user who is creating a character.
     * @param userId the user ID
     * @param message the message
     * @return true if the message was processed as part of character creation, false otherwise
     */
    public boolean processMessage(Long userId, String message) {
        CharacterCreationState state = creationStates.get(userId);

        // If the user is not in character creation, return false
        if (state == null) {
            return false;
        }

        // Process the message based on the current step
        return switch (state.getCurrentStep()) {
            case AWAITING_NAME -> {
                processNameInput(state, message);
                yield true;
            }
            case AWAITING_CLASS -> {
                processClassInput(state, message);
                yield true;
            }
            case AWAITING_GENDER -> {
                processGenderInput(state, message);
                yield true;
            }
            default ->
                // Not in a state that expects input
                false;
        };
    }

    /**
     * Process name input from the user.
     * @param state the character creation state
     * @param name the name input
     */
    private void processNameInput(CharacterCreationState state, String name) {
        // Set the name in the state
        state.setCharacterName(name);

        // Send confirmation and ask for class
        sendMessageToUserPort.messageToUser(state.getUserId(),
                "캐릭터의 이름은 " + name + "입니다. 이제 클래스를 선택하세요:");
        sendMessageToUserPort.messageToUser(state.getUserId(),
                "사용 가능한 클래스: WARRIOR(전사), MAGE(마법사), ROGUE(도적), CLERIC(성직자), RANGER(레인저)");
    }

    /**
     * Process class input from the user.
     * @param state the character creation state
     * @param classInput the class input
     */
    private void processClassInput(CharacterCreationState state, String classInput) {
        try {
            // Try to parse the class
            CharacterClass characterClass = CharacterClass.valueOf(classInput.toUpperCase());

            // Set the class in the state
            state.setCharacterClass(characterClass);

            // Ask for gender
            sendMessageToUserPort.messageToUser(state.getUserId(),
                    "캐릭터의 클래스는 " + characterClass + "입니다. 이제 성별을 선택하세요:");
            sendMessageToUserPort.messageToUser(state.getUserId(),
                    "사용 가능한 성별: MALE(남성), FEMALE(여성)");

        } catch (IllegalArgumentException e) {
            // Invalid class, ask again
            sendMessageToUserPort.messageToUser(state.getUserId(),
                    "유효하지 않은 클래스입니다. 다음 중에서 선택하세요: WARRIOR(전사), MAGE(마법사), ROGUE(도적), CLERIC(성직자), RANGER(레인저)");
        }
    }

    /**
     * Process gender input from the user.
     * @param state the character creation state
     * @param genderInput the gender input
     */
    private void processGenderInput(CharacterCreationState state, String genderInput) {
        try {
            // Try to parse the gender
            Gender gender = Gender.valueOf(genderInput.toUpperCase());

            // Set the gender in the state
            state.setCharacterGender(gender);

            // Create the character
            createCharacter(state);

        } catch (IllegalArgumentException e) {
            // Invalid gender, ask again
            sendMessageToUserPort.messageToUser(state.getUserId(),
                    "유효하지 않은 성별입니다. 다음 중에서 선택하세요: MALE(남성), FEMALE(여성)");
        }
    }

    /**
     * Create a character based on the state.
     * @param state the character creation state
     */
    private void createCharacter(CharacterCreationState state) {
        if (!state.isReadyToCreate()) {
            logger.error("Attempted to create character with incomplete state: {}", state);
            return;
        }

        // Create the character
        PlayerCharacter character = playerCharacterService.createCharacter(
                state.getUserId(),
                state.getCharacterName(),
                state.getCharacterClass(),
                state.getCharacterGender()
        );

        // Send confirmation
        sendMessageToUserPort.messageToUser(state.getUserId(),
                "캐릭터가 생성되었습니다! " + character.getCharacterClass() + " " + character.getNickname() + "님, 환영합니다.");
        sendMessageToUserPort.messageToUser(state.getUserId(),
                "현재 " + character.getBaseCharacterInfo().getRoomId() + "번 방에 있습니다.");

        // Remove the state
        creationStates.remove(state.getUserId());
    }

    /**
     * Check if a user is in character creation.
     * @param userId the user ID
     * @return true if the user is in character creation, false otherwise
     */
    public boolean isInCharacterCreation(Long userId) {
        return creationStates.containsKey(userId);
    }
}
