package com.jefflife.mudmk2.gameplay.application.service;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.BaseCharacter;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.CharacterClass;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.Gender;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.PlayableCharacter;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.PlayerCharacter;
import com.jefflife.mudmk2.gamedata.application.domain.repository.PlayerCharacterRepository;
import com.jefflife.mudmk2.gamedata.application.service.PlayerCharacterService;
import com.jefflife.mudmk2.gameplay.application.port.out.SendMessageToUserPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CharacterCreationServiceTest {

    private CharacterCreationService characterCreationService;
    private FakePlayerCharacterService fakePlayerCharacterService;
    private FakePlayerCharacterRepository fakePlayerCharacterRepository;
    private FakeSendMessageToUserPort fakeSendMessageToUserPort;

    @BeforeEach
    void setUp() {
        fakePlayerCharacterService = new FakePlayerCharacterService();
        fakePlayerCharacterRepository = new FakePlayerCharacterRepository();
        fakeSendMessageToUserPort = new FakeSendMessageToUserPort();

        characterCreationService = new CharacterCreationService(
                fakePlayerCharacterService,
                fakePlayerCharacterRepository,
                fakeSendMessageToUserPort
        );
    }

    @Test
    @DisplayName("대기 상태에서 사용자가 이름을 입력하면 이름 처리가 성공해야 함")
    void processMessage_shouldProcessNameInput_whenInAwaitingNameState() {
        // given
        Long userId = 1L;
        String characterName = "TestCharacter";

        // Start character creation to set up the state
        characterCreationService.startCharacterCreation(userId);

        // Clear messages from startCharacterCreation
        fakeSendMessageToUserPort.clearMessages();

        // when
        boolean result = characterCreationService.processMessage(userId, characterName);

        // then
        assertThat(result).isTrue();

        // Verify messages sent to user
        List<String> messages = fakeSendMessageToUserPort.getMessagesForUser(userId);
        assertThat(messages).hasSize(2);
        assertThat(messages.get(0)).contains("캐릭터의 이름은 " + characterName + "입니다");
        assertThat(messages.get(1)).contains("사용 가능한 성별");
    }

    @Test
    @DisplayName("이미 존재하는 이름을 입력하면 중복 이름 오류 메시지를 보내야 함")
    void processMessage_shouldRejectDuplicateName_whenNameAlreadyExists() {
        // given
        Long userId = 1L;
        String existingName = "ExistingCharacter";

        // Set up repository to return that the name already exists
        fakePlayerCharacterRepository.setExistsByNickname(existingName, true);

        // Start character creation to set up the state
        characterCreationService.startCharacterCreation(userId);

        // Clear messages from startCharacterCreation
        fakeSendMessageToUserPort.clearMessages();

        // when
        boolean result = characterCreationService.processMessage(userId, existingName);

        // then
        assertThat(result).isTrue();

        // Verify error message was sent
        List<String> messages = fakeSendMessageToUserPort.getMessagesForUser(userId);
        assertThat(messages).hasSize(1);
        assertThat(messages.get(0)).contains("이미 존재하는 이름입니다");
    }

    @Test
    @DisplayName("사용자가 캐릭터 생성 상태가 아닐 때 메시지 처리가 실패해야 함")
    void processMessage_shouldReturnFalse_whenUserNotInCharacterCreation() {
        // given
        Long userId = 1L;
        String message = "Hello";

        // No character creation state for this user

        // when
        boolean result = characterCreationService.processMessage(userId, message);

        // then
        assertThat(result).isFalse();

        // Verify no messages were sent
        List<String> messages = fakeSendMessageToUserPort.getMessagesForUser(userId);
        assertThat(messages).isEmpty();
    }

    // Fake implementation of PlayerCharacterService
    static class FakePlayerCharacterService extends PlayerCharacterService {
        private final Map<Long, Boolean> hasCharacterMap = new HashMap<>();
        private final Map<Long, PlayerCharacter> charactersByUserId = new HashMap<>();

        public FakePlayerCharacterService() {
            super(null, null); // Pass null for dependencies as we'll override the methods
        }

        @Override
        public boolean hasCharacter(Long userId) {
            return hasCharacterMap.getOrDefault(userId, false);
        }

        @Override
        public PlayerCharacter getCharacterByUserId(Long userId) {
            return charactersByUserId.get(userId);
        }

        @Override
        public PlayerCharacter createCharacter(Long userId, String name, CharacterClass characterClass, Gender gender) {
            // Create a simple fake PlayerCharacter
            BaseCharacter baseCharacter = BaseCharacter.builder()
                    .name(name)
                    .background("Test background")
                    .hp(100)
                    .maxHp(100)
                    .mp(50)
                    .maxMp(50)
                    .str(10)
                    .dex(10)
                    .con(10)
                    .intelligence(10)
                    .pow(10)
                    .cha(10)
                    .roomId(1L)
                    .gender(gender)
                    .build();

            PlayableCharacter playableCharacter = PlayableCharacter.builder()
                    .level(1)
                    .experience(0)
                    .nextLevelExp(100)
                    .conversable(true)
                    .build();

            // Create a mock PlayerCharacter using reflection to bypass constructor restrictions
            // This is a workaround for testing purposes only
            PlayerCharacter character;
            try {
                // Get the constructor
                java.lang.reflect.Constructor<PlayerCharacter> constructor = PlayerCharacter.class.getDeclaredConstructor(
                        UUID.class, BaseCharacter.class, PlayableCharacter.class, Long.class, 
                        String.class, CharacterClass.class, boolean.class, java.time.LocalDateTime.class
                );
                // Make it accessible
                constructor.setAccessible(true);
                // Create the instance
                character = constructor.newInstance(
                        null, // ID will be generated
                        baseCharacter,
                        playableCharacter,
                        userId,
                        name,
                        characterClass,
                        true,
                        java.time.LocalDateTime.now()
                );
            } catch (Exception e) {
                throw new RuntimeException("Failed to create PlayerCharacter for testing", e);
            }

            // Store the character
            charactersByUserId.put(userId, character);
            hasCharacterMap.put(userId, true);

            return character;
        }

        // Helper method to set up test data
        public void setHasCharacter(Long userId, boolean hasCharacter) {
            hasCharacterMap.put(userId, hasCharacter);
        }
    }

    // Fake implementation of PlayerCharacterRepository
    static class FakePlayerCharacterRepository implements PlayerCharacterRepository {
        private final Map<String, Boolean> existsByNicknameMap = new HashMap<>();
        private final Map<Long, Boolean> existsByUserIdMap = new HashMap<>();
        private final Map<Long, PlayerCharacter> charactersByUserId = new HashMap<>();
        private final Map<UUID, PlayerCharacter> charactersById = new HashMap<>();

        @Override
        public PlayerCharacter findByUserId(Long userId) {
            return charactersByUserId.get(userId);
        }

        @Override
        public boolean existsByUserId(Long userId) {
            return existsByUserIdMap.getOrDefault(userId, false);
        }

        @Override
        public boolean existsByNickname(String nickname) {
            return existsByNicknameMap.getOrDefault(nickname, false);
        }

        // Helper method to set up test data
        public void setExistsByNickname(String nickname, boolean exists) {
            existsByNicknameMap.put(nickname, exists);
        }

        public void setExistsByUserId(Long userId, boolean exists) {
            existsByUserIdMap.put(userId, exists);
        }

        // Implement required methods from CrudRepository
        @Override
        public <S extends PlayerCharacter> S save(S entity) {
            // In a real repository, the ID would be generated by the database
            // For testing, we'll just use the entity as is
            UUID id = entity.getId() != null ? entity.getId() : UUID.randomUUID();
            // We can't set the ID directly, so we'll just use it for our maps
            charactersById.put(id, entity);
            if (entity.getUserId() != null) {
                charactersByUserId.put(entity.getUserId(), entity);
                existsByUserIdMap.put(entity.getUserId(), true);
            }
            if (entity.getNickname() != null) {
                existsByNicknameMap.put(entity.getNickname(), true);
            }
            return entity;
        }

        @Override
        public <S extends PlayerCharacter> Iterable<S> saveAll(Iterable<S> entities) {
            throw new UnsupportedOperationException("Not implemented for test");
        }

        @Override
        public java.util.Optional<PlayerCharacter> findById(UUID uuid) {
            return java.util.Optional.ofNullable(charactersById.get(uuid));
        }

        @Override
        public boolean existsById(UUID uuid) {
            return charactersById.containsKey(uuid);
        }

        @Override
        public Iterable<PlayerCharacter> findAll() {
            return charactersById.values();
        }

        @Override
        public Iterable<PlayerCharacter> findAllById(Iterable<UUID> uuids) {
            throw new UnsupportedOperationException("Not implemented for test");
        }

        @Override
        public long count() {
            return charactersById.size();
        }

        @Override
        public void deleteById(UUID uuid) {
            throw new UnsupportedOperationException("Not implemented for test");
        }

        @Override
        public void delete(PlayerCharacter entity) {
            throw new UnsupportedOperationException("Not implemented for test");
        }

        @Override
        public void deleteAllById(Iterable<? extends UUID> uuids) {
            throw new UnsupportedOperationException("Not implemented for test");
        }

        @Override
        public void deleteAll(Iterable<? extends PlayerCharacter> entities) {
            throw new UnsupportedOperationException("Not implemented for test");
        }

        @Override
        public void deleteAll() {
            throw new UnsupportedOperationException("Not implemented for test");
        }
    }

    // Fake implementation of SendMessageToUserPort
    static class FakeSendMessageToUserPort implements SendMessageToUserPort {
        private final Map<Long, List<String>> messagesByUserId = new HashMap<>();

        @Override
        public void messageToUser(Long userId, String content) {
            messagesByUserId.computeIfAbsent(userId, k -> new ArrayList<>()).add(content);
        }

        // Helper method to get messages sent to a user
        public List<String> getMessagesForUser(Long userId) {
            return messagesByUserId.getOrDefault(userId, new ArrayList<>());
        }

        // Helper method to clear messages
        public void clearMessages() {
            messagesByUserId.clear();
        }
    }
}
