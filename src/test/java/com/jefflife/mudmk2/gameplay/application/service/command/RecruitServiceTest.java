package com.jefflife.mudmk2.gameplay.application.service.command;

import com.jefflife.mudmk2.gamedata.application.domain.model.party.Party;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.NonPlayerCharacter;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.PlayerCharacter;
import com.jefflife.mudmk2.gameplay.application.domain.model.command.RecruitCommand;
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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecruitServiceTest {

    @Mock
    private GameWorldService gameWorldService;

    @Mock
    private SendMessageToUserPort sendMessageToUserPort;

    @Captor
    private ArgumentCaptor<String> messageCaptor;

    @Captor
    private ArgumentCaptor<Long> userIdCaptor;

    private RecruitService recruitService;

    private static final Long USER_ID = 1L;
    private static final Long PLAYER_ID = 1L;
    private static final Long NPC_ID = 2L;
    private static final Long ROOM_ID = 100L;
    private static final String NPC_NAME = "테스트NPC";

    @BeforeEach
    void setUp() {
        recruitService = new RecruitService(gameWorldService, sendMessageToUserPort);
    }

    @Nested
    @DisplayName("NPC 초대 테스트")
    @ExtendWith(MockitoExtension.class)
    class RecruitTests {

        @BeforeEach
        void setUp() {
            // lenient 모드를 설정하여 "Unnecessary stubbings detected" 경고를 제거합니다.
            lenient().when(mock(Party.class).isLeader(anyLong())).thenReturn(true);
        }

        @Test
        @DisplayName("NPC를 찾을 수 없는 경우 적절한 메시지를 보낸다")
        void shouldSendMessageWhenNpcNotFound() {
            // given
            RecruitCommand command = new RecruitCommand(USER_ID, NPC_NAME);
            PlayerCharacter player = mock(PlayerCharacter.class);

            lenient().when(player.getId()).thenReturn(PLAYER_ID);
            lenient().when(player.getCurrentRoomId()).thenReturn(ROOM_ID);
            lenient().when(gameWorldService.getPlayerByUserId(USER_ID)).thenReturn(player);
            lenient().when(gameWorldService.getNpcByName(NPC_NAME)).thenReturn(null);

            // when
            recruitService.recruit(command);

            // then
            verify(sendMessageToUserPort).messageToUser(userIdCaptor.capture(), messageCaptor.capture());
            assertThat(userIdCaptor.getValue()).isEqualTo(USER_ID);
            assertThat(messageCaptor.getValue()).contains("대상을 찾을 수 없습니다");
        }

        @Test
        @DisplayName("NPC가 같은 방에 없는 경우 적절한 메시지를 보낸다")
        void shouldSendMessageWhenNpcInDifferentRoom() {
            // given
            RecruitCommand command = new RecruitCommand(USER_ID, NPC_NAME);
            PlayerCharacter player = mock(PlayerCharacter.class);
            NonPlayerCharacter npc = mock(NonPlayerCharacter.class);

            lenient().when(player.getId()).thenReturn(PLAYER_ID);
            lenient().when(player.getCurrentRoomId()).thenReturn(ROOM_ID);
            lenient().when(npc.getId()).thenReturn(NPC_ID);
            lenient().when(npc.getName()).thenReturn(NPC_NAME);
            lenient().when(npc.getCurrentRoomId()).thenReturn(ROOM_ID + 1); // 다른 방

            lenient().when(gameWorldService.getPlayerByUserId(USER_ID)).thenReturn(player);
            lenient().when(gameWorldService.getNpcByName(NPC_NAME)).thenReturn(npc);

            // when
            recruitService.recruit(command);

            // then
            verify(sendMessageToUserPort).messageToUser(userIdCaptor.capture(), messageCaptor.capture());
            assertThat(userIdCaptor.getValue()).isEqualTo(USER_ID);
            assertThat(messageCaptor.getValue()).contains("같은 방에 있지 않습니다");
        }

        @Test
        @DisplayName("NPC가 이미 다른 파티에 속해 있는 경우 적절한 메시지를 보낸다")
        void shouldSendMessageWhenNpcInOtherParty() {
            // given
            RecruitCommand command = new RecruitCommand(USER_ID, NPC_NAME);
            PlayerCharacter player = mock(PlayerCharacter.class);
            NonPlayerCharacter npc = mock(NonPlayerCharacter.class);

            lenient().when(player.getId()).thenReturn(PLAYER_ID);
            lenient().when(player.getCurrentRoomId()).thenReturn(ROOM_ID);
            lenient().when(npc.getId()).thenReturn(NPC_ID);
            lenient().when(npc.getName()).thenReturn(NPC_NAME);
            lenient().when(npc.getCurrentRoomId()).thenReturn(ROOM_ID); // 같은 방

            lenient().when(gameWorldService.getPlayerByUserId(USER_ID)).thenReturn(player);
            lenient().when(gameWorldService.getNpcByName(NPC_NAME)).thenReturn(npc);
            lenient().when(gameWorldService.isInParty(NPC_ID)).thenReturn(true); // 이미 파티에 속해 있음

            // when
            recruitService.recruit(command);

            // then
            verify(sendMessageToUserPort).messageToUser(userIdCaptor.capture(), messageCaptor.capture());
            assertThat(userIdCaptor.getValue()).isEqualTo(USER_ID);
            assertThat(messageCaptor.getValue()).contains("이미 다른 파티에 속해 있습니다");
        }

        @Test
        @DisplayName("파티 리더가 아닌 경우 적절한 메시지를 보낸다")
        void shouldSendMessageWhenNotPartyLeader() {
            // given
            RecruitCommand command = new RecruitCommand(USER_ID, NPC_NAME);
            PlayerCharacter player = mock(PlayerCharacter.class);
            NonPlayerCharacter npc = mock(NonPlayerCharacter.class);
            Party party = mock(Party.class);

            lenient().when(player.getId()).thenReturn(PLAYER_ID);
            lenient().when(player.getCurrentRoomId()).thenReturn(ROOM_ID);
            lenient().when(npc.getId()).thenReturn(NPC_ID);
            lenient().when(npc.getName()).thenReturn(NPC_NAME);
            lenient().when(npc.getCurrentRoomId()).thenReturn(ROOM_ID);
            lenient().when(party.isLeader(PLAYER_ID)).thenReturn(false); // 리더가 아님

            lenient().when(gameWorldService.getPlayerByUserId(USER_ID)).thenReturn(player);
            lenient().when(gameWorldService.getNpcByName(NPC_NAME)).thenReturn(npc);
            lenient().when(gameWorldService.isInParty(NPC_ID)).thenReturn(false);
            lenient().when(gameWorldService.getPartyByPlayerId(PLAYER_ID)).thenReturn(Optional.of(party));

            // when
            recruitService.recruit(command);

            // then
            verify(sendMessageToUserPort).messageToUser(userIdCaptor.capture(), messageCaptor.capture());
            assertThat(userIdCaptor.getValue()).isEqualTo(USER_ID);
            assertThat(messageCaptor.getValue()).contains("파티 리더만 NPC를 초대할 수 있습니다");
        }

        @Test
        @DisplayName("파티가 가득 찬 경우 적절한 메시지를 보낸다")
        void shouldSendMessageWhenPartyIsFull() {
            // given
            RecruitCommand command = new RecruitCommand(USER_ID, NPC_NAME);
            PlayerCharacter player = mock(PlayerCharacter.class);
            NonPlayerCharacter npc = mock(NonPlayerCharacter.class);
            Party party = mock(Party.class);

            lenient().when(player.getId()).thenReturn(PLAYER_ID);
            lenient().when(player.getCurrentRoomId()).thenReturn(ROOM_ID);
            lenient().when(npc.getId()).thenReturn(NPC_ID);
            lenient().when(npc.getName()).thenReturn(NPC_NAME);
            lenient().when(npc.getCurrentRoomId()).thenReturn(ROOM_ID);
            lenient().when(party.isLeader(PLAYER_ID)).thenReturn(true); // 리더임
            lenient().when(party.addMember(NPC_ID)).thenReturn(Party.AddPartyMemberResult.PARTY_FULL);

            lenient().when(gameWorldService.getPlayerByUserId(USER_ID)).thenReturn(player);
            lenient().when(gameWorldService.getNpcByName(NPC_NAME)).thenReturn(npc);
            lenient().when(gameWorldService.isInParty(NPC_ID)).thenReturn(false);
            lenient().when(gameWorldService.getPartyByPlayerId(PLAYER_ID)).thenReturn(Optional.of(party));

            // when
            recruitService.recruit(command);

            // then
            verify(sendMessageToUserPort).messageToUser(userIdCaptor.capture(), messageCaptor.capture());
            assertThat(userIdCaptor.getValue()).isEqualTo(USER_ID);
            assertThat(messageCaptor.getValue()).contains("파티가 가득 찼습니다");
        }

        @Test
        @DisplayName("NPC가 이미 같은 파티에 있는 경우 적절한 메시지를 보낸다")
        void shouldSendMessageWhenNpcAlreadyInSameParty() {
            // given
            RecruitCommand command = new RecruitCommand(USER_ID, NPC_NAME);
            PlayerCharacter player = mock(PlayerCharacter.class);
            NonPlayerCharacter npc = mock(NonPlayerCharacter.class);
            Party party = mock(Party.class);

            lenient().when(player.getId()).thenReturn(PLAYER_ID);
            lenient().when(player.getCurrentRoomId()).thenReturn(ROOM_ID);
            lenient().when(npc.getId()).thenReturn(NPC_ID);
            lenient().when(npc.getName()).thenReturn(NPC_NAME);
            lenient().when(npc.getCurrentRoomId()).thenReturn(ROOM_ID);
            lenient().when(party.isLeader(PLAYER_ID)).thenReturn(true); // 리더임
            lenient().when(party.addMember(NPC_ID)).thenReturn(Party.AddPartyMemberResult.ALREADY_IN_SAME_PARTY);

            lenient().when(gameWorldService.getPlayerByUserId(USER_ID)).thenReturn(player);
            lenient().when(gameWorldService.getNpcByName(NPC_NAME)).thenReturn(npc);
            lenient().when(gameWorldService.isInParty(NPC_ID)).thenReturn(false);
            lenient().when(gameWorldService.getPartyByPlayerId(PLAYER_ID)).thenReturn(Optional.of(party));

            // when
            recruitService.recruit(command);

            // then
            verify(sendMessageToUserPort).messageToUser(userIdCaptor.capture(), messageCaptor.capture());
            assertThat(userIdCaptor.getValue()).isEqualTo(USER_ID);
            assertThat(messageCaptor.getValue()).contains("이미 당신의 파티에 있습니다");
        }

        @Test
        @DisplayName("NPC가 성공적으로 파티에 초대된 경우 적절한 메시지를 보낸다")
        void shouldSendSuccessMessageWhenNpcJoinsParty() {
            // given
            RecruitCommand command = new RecruitCommand(USER_ID, NPC_NAME);
            PlayerCharacter player = mock(PlayerCharacter.class);
            NonPlayerCharacter npc = mock(NonPlayerCharacter.class);
            Party party = mock(Party.class);

            lenient().when(player.getId()).thenReturn(PLAYER_ID);
            lenient().when(player.getCurrentRoomId()).thenReturn(ROOM_ID);
            lenient().when(npc.getId()).thenReturn(NPC_ID);
            lenient().when(npc.getName()).thenReturn(NPC_NAME);
            lenient().when(npc.getCurrentRoomId()).thenReturn(ROOM_ID);
            lenient().when(party.isLeader(PLAYER_ID)).thenReturn(true); // 리더임
            lenient().when(party.addMember(NPC_ID)).thenReturn(Party.AddPartyMemberResult.SUCCESS);

            lenient().when(gameWorldService.getPlayerByUserId(USER_ID)).thenReturn(player);
            lenient().when(gameWorldService.getNpcByName(NPC_NAME)).thenReturn(npc);
            lenient().when(gameWorldService.isInParty(NPC_ID)).thenReturn(false);
            lenient().when(gameWorldService.getPartyByPlayerId(PLAYER_ID)).thenReturn(Optional.of(party));

            // when
            recruitService.recruit(command);

            // then
            verify(sendMessageToUserPort).messageToUser(userIdCaptor.capture(), messageCaptor.capture());
            assertThat(userIdCaptor.getValue()).isEqualTo(USER_ID);
            assertThat(messageCaptor.getValue()).contains("가 당신의 파티에 합류했습니다");
        }

        @Test
        @DisplayName("플레이어가 파티가 없는 경우 새로운 파티를 생성하고 NPC를 초대한다")
        void shouldCreateNewPartyWhenPlayerHasNoParty() {
            // given
            RecruitCommand command = new RecruitCommand(USER_ID, NPC_NAME);
            PlayerCharacter player = mock(PlayerCharacter.class);
            NonPlayerCharacter npc = mock(NonPlayerCharacter.class);
            Party newParty = mock(Party.class);

            lenient().when(player.getId()).thenReturn(PLAYER_ID);
            lenient().when(player.getCurrentRoomId()).thenReturn(ROOM_ID);
            lenient().when(npc.getId()).thenReturn(NPC_ID);
            lenient().when(npc.getName()).thenReturn(NPC_NAME);
            lenient().when(npc.getCurrentRoomId()).thenReturn(ROOM_ID);
            lenient().when(newParty.isLeader(PLAYER_ID)).thenReturn(true);
            lenient().when(newParty.addMember(NPC_ID)).thenReturn(Party.AddPartyMemberResult.SUCCESS);

            lenient().when(gameWorldService.getPlayerByUserId(USER_ID)).thenReturn(player);
            lenient().when(gameWorldService.getNpcByName(NPC_NAME)).thenReturn(npc);
            lenient().when(gameWorldService.isInParty(NPC_ID)).thenReturn(false);
            lenient().when(gameWorldService.getPartyByPlayerId(PLAYER_ID)).thenReturn(Optional.empty()); // 파티 없음

            // void 메서드이므로 doAnswer().when() 패턴을 사용
            try (var partyMockedStatic = mockStatic(Party.class)) {
                partyMockedStatic.when(() -> Party.createParty(PLAYER_ID)).thenReturn(newParty);

                // when
                recruitService.recruit(command);

                // then
                verify(gameWorldService).addParty(newParty);
                verify(sendMessageToUserPort).messageToUser(userIdCaptor.capture(), messageCaptor.capture());
                assertThat(userIdCaptor.getValue()).isEqualTo(USER_ID);
                assertThat(messageCaptor.getValue()).contains("가 당신의 파티에 합류했습니다");
            }
        }
    }
}
