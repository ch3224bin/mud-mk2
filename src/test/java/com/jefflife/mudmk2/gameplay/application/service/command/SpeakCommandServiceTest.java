package com.jefflife.mudmk2.gameplay.application.service.command;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.NonPlayerCharacter;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.PlayerCharacter;
import com.jefflife.mudmk2.gameplay.application.domain.model.command.SpeakCommand;
import com.jefflife.mudmk2.gameplay.application.port.out.SendMessageToUserPort;
import com.jefflife.mudmk2.gameplay.application.service.GameWorldService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

class SpeakCommandServiceTest {

    private static final Long USER_ID = 1L;
    private static final Long OTHER_USER_ID = 2L;
    private static final Long ROOM_ID = 101L;
    private static final UUID PLAYER_ID = UUID.randomUUID();
    private static final UUID OTHER_PLAYER_ID = UUID.randomUUID();
    private static final UUID NPC_ID = UUID.randomUUID();
    private static final String PLAYER_NAME = "플레이어";
    private static final String OTHER_PLAYER_NAME = "다른플레이어";
    private static final String NPC_NAME = "NPC";
    private static final String MESSAGE = "안녕하세요";

    private FakeGameWorldService gameWorldService;
    private FakeMessageSender messageSender;
    private SpeakCommandService speakCommandService;

    @BeforeEach
    void setUp() {
        gameWorldService = new FakeGameWorldService();
        messageSender = new FakeMessageSender();
        speakCommandService = new SpeakCommandService(gameWorldService, messageSender);
    }

    @Nested
    @DisplayName("말하기 테스트")
    class SpeakTests {

        @BeforeEach
        void setUp() {
            messageSender.clear();
        }

        @Test
        @DisplayName("타겟 없이 말하면 방에 있는 모든 플레이어에게 메시지를 보낸다")
        void shouldSendMessageToAllPlayersInRoomWhenNoTarget() {
            // given
            FakePlayerCharacter player = new FakePlayerCharacter(PLAYER_ID, PLAYER_NAME, USER_ID, ROOM_ID);
            FakePlayerCharacter otherPlayer = new FakePlayerCharacter(OTHER_PLAYER_ID, OTHER_PLAYER_NAME, OTHER_USER_ID, ROOM_ID);

            gameWorldService.addPlayer(player);
            gameWorldService.addPlayer(otherPlayer);

            SpeakCommand command = new SpeakCommand(USER_ID, null, MESSAGE);

            // when
            speakCommandService.speak(command);

            // then
            List<FakeMessageSender.Message> sentMessages = messageSender.getSentMessages();

            // 화자에게는 "당신은 ~라고 말합니다" 메시지가 전송됨
            List<FakeMessageSender.Message> speakerMessages = sentMessages.stream()
                    .filter(msg -> msg.userId.equals(USER_ID))
                    .toList();
            assertThat(speakerMessages).hasSize(1);
            assertThat(speakerMessages.getFirst().content).isEqualTo(String.format("당신은 \"%s\"라고 말합니다.", MESSAGE));

            // 다른 플레이어에게는 "플레이어가 ~라고 말합니다" 메시지가 전송됨
            List<FakeMessageSender.Message> otherPlayerMessages = sentMessages.stream()
                    .filter(msg -> msg.userId.equals(OTHER_USER_ID))
                    .toList();
            assertThat(otherPlayerMessages).hasSize(1);
            assertThat(otherPlayerMessages.getFirst().content).isEqualTo(String.format("%s이(가) \"%s\"라고 말합니다", PLAYER_NAME, MESSAGE));
        }

        @Test
        @DisplayName("NPC를 타겟으로 말하면 방에 있는 모든 플레이어에게 타겟을 포함한 메시지를 보낸다")
        void shouldSendMessageToAllPlayersInRoomWhenTargetIsNpc() {
            // given
            FakePlayerCharacter player = new FakePlayerCharacter(PLAYER_ID, PLAYER_NAME, USER_ID, ROOM_ID);
            FakePlayerCharacter otherPlayer = new FakePlayerCharacter(OTHER_PLAYER_ID, OTHER_PLAYER_NAME, OTHER_USER_ID, ROOM_ID);
            FakeNonPlayerCharacter npc = new FakeNonPlayerCharacter(NPC_ID, NPC_NAME, ROOM_ID);

            gameWorldService.addPlayer(player);
            gameWorldService.addPlayer(otherPlayer);
            gameWorldService.addNpc(npc);

            SpeakCommand command = new SpeakCommand(USER_ID, NPC_NAME, MESSAGE);

            // when
            speakCommandService.speak(command);

            // then
            List<FakeMessageSender.Message> sentMessages = messageSender.getSentMessages();

            // 화자에게는 "당신은 NPC에게 ~라고 말합니다" 메시지가 전송됨
            List<FakeMessageSender.Message> speakerMessages = sentMessages.stream()
                    .filter(msg -> msg.userId.equals(USER_ID))
                    .toList();
            assertThat(speakerMessages).hasSize(1);
            assertThat(speakerMessages.getFirst().content).isEqualTo(String.format("당신은 %s에게 \"%s\"라고 말합니다.", NPC_NAME, MESSAGE));

            // 다른 플레이어에게는 "플레이어가 NPC에게 ~라고 말합니다" 메시지가 전송됨
            List<FakeMessageSender.Message> otherPlayerMessages = sentMessages.stream()
                    .filter(msg -> msg.userId.equals(OTHER_USER_ID))
                    .toList();
            assertThat(otherPlayerMessages).hasSize(1);
            assertThat(otherPlayerMessages.getFirst().content).isEqualTo(String.format("%s이(가) %s에게 \"%s\"라고 말합니다", PLAYER_NAME, NPC_NAME, MESSAGE));
        }

        @Test
        @DisplayName("PC를 타겟으로 말하면 방에 있는 모든 플레이어에게 타겟을 포함한 메시지를 보낸다")
        void shouldSendMessageToAllPlayersInRoomWhenTargetIsPlayer() {
            // given
            FakePlayerCharacter player = new FakePlayerCharacter(PLAYER_ID, PLAYER_NAME, USER_ID, ROOM_ID);
            FakePlayerCharacter otherPlayer = new FakePlayerCharacter(OTHER_PLAYER_ID, OTHER_PLAYER_NAME, OTHER_USER_ID, ROOM_ID);

            gameWorldService.addPlayer(player);
            gameWorldService.addPlayer(otherPlayer);

            SpeakCommand command = new SpeakCommand(USER_ID, OTHER_PLAYER_NAME, MESSAGE);

            // when
            speakCommandService.speak(command);

            // then
            List<FakeMessageSender.Message> sentMessages = messageSender.getSentMessages();

            // 화자에게는 "당신은 타겟에게 ~라고 말합니다" 메시지가 전송됨
            List<FakeMessageSender.Message> speakerMessages = sentMessages.stream()
                    .filter(msg -> msg.userId.equals(USER_ID))
                    .toList();
            assertThat(speakerMessages).hasSize(1);
            assertThat(speakerMessages.getFirst().content).isEqualTo(String.format("당신은 %s에게 \"%s\"라고 말합니다.", OTHER_PLAYER_NAME, MESSAGE));

            // 타겟 플레이어에게는 "플레이어가 당신에게 ~라고 말합니다" 메시지가 전송됨
            List<FakeMessageSender.Message> targetMessages = sentMessages.stream()
                    .filter(msg -> msg.userId.equals(OTHER_USER_ID))
                    .toList();
            assertThat(targetMessages).hasSize(1);
            assertThat(targetMessages.getFirst().content).isEqualTo(String.format("%s이(가) 당신에게 \"%s\"라고 말합니다.", PLAYER_NAME, MESSAGE));
        }

        @Test
        @DisplayName("타겟이 방에 없으면 플레이어에게 오류 메시지를 보낸다")
        void shouldSendErrorMessageWhenTargetNotInRoom() {
            // given
            String targetName = "존재하지않는타겟";
            FakePlayerCharacter player = new FakePlayerCharacter(PLAYER_ID, PLAYER_NAME, USER_ID, ROOM_ID);

            gameWorldService.addPlayer(player);

            SpeakCommand command = new SpeakCommand(USER_ID, targetName, MESSAGE);

            // when
            speakCommandService.speak(command);

            // then
            List<FakeMessageSender.Message> sentMessages = messageSender.getSentMessages();

            // 화자에게 오류 메시지가 전송됨
            List<FakeMessageSender.Message> errorMessages = sentMessages.stream()
                    .filter(msg -> msg.userId.equals(USER_ID) && msg.content.equals(targetName + "은(는) 이 방안에 없습니다."))
                    .toList();
            assertThat(errorMessages).hasSize(1);
        }

        @Test
        @DisplayName("NPC가 다른 방에 있으면 플레이어에게 오류 메시지를 보낸다")
        void shouldSendErrorMessageWhenNpcInDifferentRoom() {
            // given
            FakePlayerCharacter player = new FakePlayerCharacter(PLAYER_ID, PLAYER_NAME, USER_ID, ROOM_ID);
            FakeNonPlayerCharacter npc = new FakeNonPlayerCharacter(NPC_ID, NPC_NAME, ROOM_ID + 1); // 다른 방

            gameWorldService.addPlayer(player);
            gameWorldService.addNpc(npc);

            SpeakCommand command = new SpeakCommand(USER_ID, NPC_NAME, MESSAGE);

            // when
            speakCommandService.speak(command);

            // then
            List<FakeMessageSender.Message> sentMessages = messageSender.getSentMessages();

            // 화자에게 오류 메시지가 전송됨
            List<FakeMessageSender.Message> errorMessages = sentMessages.stream()
                    .filter(msg -> msg.userId.equals(USER_ID) && msg.content.equals(NPC_NAME + "은(는) 이 방안에 없습니다."))
                    .toList();
            assertThat(errorMessages).hasSize(1);
        }
    }

    // Fake implementation of GameWorldService
    static class FakeGameWorldService extends GameWorldService {
        private final Map<Long, PlayerCharacter> playersByUserId = new HashMap<>();
        private final Map<String, NonPlayerCharacter> npcsByName = new HashMap<>();
        private final Map<Long, List<PlayerCharacter>> playersByRoomId = new HashMap<>();
        private final Map<String, PlayerCharacter> playersByName = new HashMap<>();

        public void addPlayer(PlayerCharacter player) {
            playersByUserId.put(player.getUserId(), player);
            playersByRoomId.computeIfAbsent(player.getCurrentRoomId(), k -> new ArrayList<>()).add(player);
            playersByName.put(player.getName(), player);
        }

        public void addNpc(NonPlayerCharacter npc) {
            npcsByName.put(npc.getName(), npc);
        }

        @Override
        public PlayerCharacter getPlayerByUserId(Long userId) {
            return playersByUserId.get(userId);
        }

        @Override
        public PlayerCharacter getPlayerByName(String name) {
            return playersByName.get(name);
        }

        @Override
        public NonPlayerCharacter getNpcByName(String name) {
            return npcsByName.get(name);
        }

        @Override
        public List<PlayerCharacter> getPlayersInRoom(Long roomId) {
            return playersByRoomId.getOrDefault(roomId, Collections.emptyList());
        }
    }

    // Fake implementation of SendMessageToUserPort
    static class FakeMessageSender implements SendMessageToUserPort {
        private final List<Message> sentMessages = new ArrayList<>();

        static class Message {
            final Long userId;
            final String content;

            Message(Long userId, String content) {
                this.userId = userId;
                this.content = content;
            }
        }

        @Override
        public void messageToUser(Long userId, String content) {
            sentMessages.add(new Message(userId, content));
        }

        public List<Message> getSentMessages() {
            return sentMessages;
        }

        public void clear() {
            sentMessages.clear();
        }
    }

    // Fake implementation of PlayerCharacter
    static class FakePlayerCharacter extends PlayerCharacter {
        private final UUID id;
        private final String name;
        private final Long userId;
        private Long roomId;

        public FakePlayerCharacter(UUID id, String name, Long userId, Long roomId) {
            this.id = id;
            this.name = name;
            this.userId = userId;
            this.roomId = roomId;
        }

        @Override
        public UUID getId() {
            return id;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public Long getUserId() {
            return userId;
        }

        @Override
        public Long getCurrentRoomId() {
            return roomId;
        }

        @Override
        public void setCurrentRoomId(Long roomId) {
            this.roomId = roomId;
        }
    }

    // Fake implementation of NonPlayerCharacter
    static class FakeNonPlayerCharacter extends NonPlayerCharacter {
        private final UUID id;
        private final String name;
        private final Long roomId;

        public FakeNonPlayerCharacter(UUID id, String name, Long roomId) {
            this.id = id;
            this.name = name;
            this.roomId = roomId;
        }

        @Override
        public UUID getId() {
            return id;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public Long getCurrentRoomId() {
            return roomId;
        }
    }
}
