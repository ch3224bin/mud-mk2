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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpeakServiceTest {

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

    @Mock
    private GameWorldService gameWorldService;

    @Mock
    private SendMessageToUserPort sendMessageToUserPort;

    @Captor
    private ArgumentCaptor<Long> userIdCaptor;

    @Captor
    private ArgumentCaptor<String> messageCaptor;

    private SpeakService speakService;

    @BeforeEach
    void setUp() {
        speakService = new SpeakService(gameWorldService, sendMessageToUserPort);
    }

    @Nested
    @DisplayName("말하기 테스트")
    class SpeakTests {

        @Test
        @DisplayName("타겟 없이 말하면 방에 있는 모든 플레이어에게 메시지를 보낸다")
        void shouldSendMessageToAllPlayersInRoomWhenNoTarget() {
            // given
            SpeakCommand command = new SpeakCommand(USER_ID, null, MESSAGE);
            PlayerCharacter player = mock(PlayerCharacter.class);
            PlayerCharacter otherPlayer = mock(PlayerCharacter.class);
            List<PlayerCharacter> playersInRoom = Arrays.asList(player, otherPlayer);

            lenient().when(player.getId()).thenReturn(PLAYER_ID);
            lenient().when(player.getName()).thenReturn(PLAYER_NAME);
            lenient().when(player.getCurrentRoomId()).thenReturn(ROOM_ID);
            lenient().when(player.getUserId()).thenReturn(USER_ID);

            lenient().when(otherPlayer.getId()).thenReturn(OTHER_PLAYER_ID);
            lenient().when(otherPlayer.getName()).thenReturn(OTHER_PLAYER_NAME);
            lenient().when(otherPlayer.getCurrentRoomId()).thenReturn(ROOM_ID);
            lenient().when(otherPlayer.getUserId()).thenReturn(OTHER_USER_ID);

            lenient().when(gameWorldService.getPlayerByUserId(USER_ID)).thenReturn(player);
            lenient().when(gameWorldService.getPlayersInRoom(ROOM_ID)).thenReturn(playersInRoom);

            // when
            speakService.speak(command);

            // then
            verify(sendMessageToUserPort, times(2)).messageToUser(userIdCaptor.capture(), messageCaptor.capture());
            List<Long> capturedUserIds = userIdCaptor.getAllValues();
            List<String> capturedMessages = messageCaptor.getAllValues();

            assertThat(capturedUserIds).contains(USER_ID, OTHER_USER_ID);
            for (String message : capturedMessages) {
                assertThat(message).isEqualTo(PLAYER_NAME + "가 \"" + MESSAGE + "\"라고 말합니다");
            }
        }

        @Test
        @DisplayName("NPC를 타겟으로 말하면 방에 있는 모든 플레이어에게 타겟을 포함한 메시지를 보낸다")
        void shouldSendMessageToAllPlayersInRoomWhenTargetIsNpc() {
            // given
            SpeakCommand command = new SpeakCommand(USER_ID, NPC_NAME, MESSAGE);
            PlayerCharacter player = mock(PlayerCharacter.class);
            PlayerCharacter otherPlayer = mock(PlayerCharacter.class);
            NonPlayerCharacter npc = mock(NonPlayerCharacter.class);
            List<PlayerCharacter> playersInRoom = Arrays.asList(player, otherPlayer);

            lenient().when(player.getId()).thenReturn(PLAYER_ID);
            lenient().when(player.getName()).thenReturn(PLAYER_NAME);
            lenient().when(player.getCurrentRoomId()).thenReturn(ROOM_ID);
            lenient().when(player.getUserId()).thenReturn(USER_ID);

            lenient().when(otherPlayer.getId()).thenReturn(OTHER_PLAYER_ID);
            lenient().when(otherPlayer.getName()).thenReturn(OTHER_PLAYER_NAME);
            lenient().when(otherPlayer.getCurrentRoomId()).thenReturn(ROOM_ID);
            lenient().when(otherPlayer.getUserId()).thenReturn(OTHER_USER_ID);

            lenient().when(npc.getId()).thenReturn(NPC_ID);
            lenient().when(npc.getName()).thenReturn(NPC_NAME);
            lenient().when(npc.getCurrentRoomId()).thenReturn(ROOM_ID);

            lenient().when(gameWorldService.getPlayerByUserId(USER_ID)).thenReturn(player);
            lenient().when(gameWorldService.getNpcByName(NPC_NAME)).thenReturn(npc);
            lenient().when(gameWorldService.getPlayersInRoom(ROOM_ID)).thenReturn(playersInRoom);

            // when
            speakService.speak(command);

            // then
            verify(sendMessageToUserPort, times(2)).messageToUser(userIdCaptor.capture(), messageCaptor.capture());
            List<Long> capturedUserIds = userIdCaptor.getAllValues();
            List<String> capturedMessages = messageCaptor.getAllValues();

            assertThat(capturedUserIds).contains(USER_ID, OTHER_USER_ID);
            for (String message : capturedMessages) {
                assertThat(message).isEqualTo(PLAYER_NAME + "가 " + NPC_NAME + "에게 \"" + MESSAGE + "\"라고 말합니다");
            }
        }

        @Test
        @DisplayName("PC를 타겟으로 말하면 방에 있는 모든 플레이어에게 타겟을 포함한 메시지를 보낸다")
        void shouldSendMessageToAllPlayersInRoomWhenTargetIsPlayer() {
            // given
            SpeakCommand command = new SpeakCommand(USER_ID, OTHER_PLAYER_NAME, MESSAGE);
            PlayerCharacter player = mock(PlayerCharacter.class);
            PlayerCharacter otherPlayer = mock(PlayerCharacter.class);
            List<PlayerCharacter> playersInRoom = Arrays.asList(player, otherPlayer);

            lenient().when(player.getId()).thenReturn(PLAYER_ID);
            lenient().when(player.getName()).thenReturn(PLAYER_NAME);
            lenient().when(player.getCurrentRoomId()).thenReturn(ROOM_ID);
            lenient().when(player.getUserId()).thenReturn(USER_ID);

            lenient().when(otherPlayer.getId()).thenReturn(OTHER_PLAYER_ID);
            lenient().when(otherPlayer.getName()).thenReturn(OTHER_PLAYER_NAME);
            lenient().when(otherPlayer.getCurrentRoomId()).thenReturn(ROOM_ID);
            lenient().when(otherPlayer.getUserId()).thenReturn(OTHER_USER_ID);

            lenient().when(gameWorldService.getPlayerByUserId(USER_ID)).thenReturn(player);
            lenient().when(gameWorldService.getNpcByName(OTHER_PLAYER_NAME)).thenReturn(null);
            lenient().when(gameWorldService.getPlayersInRoom(ROOM_ID)).thenReturn(playersInRoom);

            // when
            speakService.speak(command);

            // then
            verify(sendMessageToUserPort, times(2)).messageToUser(userIdCaptor.capture(), messageCaptor.capture());
            List<Long> capturedUserIds = userIdCaptor.getAllValues();
            List<String> capturedMessages = messageCaptor.getAllValues();

            assertThat(capturedUserIds).contains(USER_ID, OTHER_USER_ID);
            for (String message : capturedMessages) {
                assertThat(message).isEqualTo(PLAYER_NAME + "가 " + OTHER_PLAYER_NAME + "에게 \"" + MESSAGE + "\"라고 말합니다");
            }
        }

        @Test
        @DisplayName("타겟이 방에 없으면 플레이어에게 오류 메시지를 보낸다")
        void shouldSendErrorMessageWhenTargetNotInRoom() {
            // given
            String targetName = "존재하지않는타겟";
            SpeakCommand command = new SpeakCommand(USER_ID, targetName, MESSAGE);
            PlayerCharacter player = mock(PlayerCharacter.class);
            List<PlayerCharacter> playersInRoom = Arrays.asList(player);

            lenient().when(player.getId()).thenReturn(PLAYER_ID);
            lenient().when(player.getName()).thenReturn(PLAYER_NAME);
            lenient().when(player.getCurrentRoomId()).thenReturn(ROOM_ID);
            lenient().when(player.getUserId()).thenReturn(USER_ID);

            lenient().when(gameWorldService.getPlayerByUserId(USER_ID)).thenReturn(player);
            lenient().when(gameWorldService.getNpcByName(targetName)).thenReturn(null);
            lenient().when(gameWorldService.getPlayersInRoom(ROOM_ID)).thenReturn(playersInRoom);

            // when
            speakService.speak(command);

            // then
            verify(sendMessageToUserPort).messageToUser(userIdCaptor.capture(), messageCaptor.capture());
            assertThat(userIdCaptor.getValue()).isEqualTo(USER_ID);
            assertThat(messageCaptor.getValue()).isEqualTo(targetName + "은 이 방안에 없습니다.");
        }

        @Test
        @DisplayName("NPC가 다른 방에 있으면 플레이어에게 오류 메시지를 보낸다")
        void shouldSendErrorMessageWhenNpcInDifferentRoom() {
            // given
            SpeakCommand command = new SpeakCommand(USER_ID, NPC_NAME, MESSAGE);
            PlayerCharacter player = mock(PlayerCharacter.class);
            NonPlayerCharacter npc = mock(NonPlayerCharacter.class);

            lenient().when(player.getId()).thenReturn(PLAYER_ID);
            lenient().when(player.getName()).thenReturn(PLAYER_NAME);
            lenient().when(player.getCurrentRoomId()).thenReturn(ROOM_ID);
            lenient().when(player.getUserId()).thenReturn(USER_ID);

            lenient().when(npc.getId()).thenReturn(NPC_ID);
            lenient().when(npc.getName()).thenReturn(NPC_NAME);
            lenient().when(npc.getCurrentRoomId()).thenReturn(ROOM_ID + 1); // 다른 방

            lenient().when(gameWorldService.getPlayerByUserId(USER_ID)).thenReturn(player);
            lenient().when(gameWorldService.getNpcByName(NPC_NAME)).thenReturn(npc);

            // when
            speakService.speak(command);

            // then
            verify(sendMessageToUserPort).messageToUser(userIdCaptor.capture(), messageCaptor.capture());
            assertThat(userIdCaptor.getValue()).isEqualTo(USER_ID);
            assertThat(messageCaptor.getValue()).isEqualTo(NPC_NAME + "은 이 방안에 없습니다.");
        }
    }
}
