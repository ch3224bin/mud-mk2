package com.jefflife.mudmk2.gamedata.application.domain.model.party;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PartyTest {

    @Nested
    @DisplayName("createParty 메소드는")
    class CreatePartyTest {

        @Test
        @DisplayName("파티를 생성하고 리더를 파티에 추가한다")
        void createParty_ShouldCreatePartyAndAddLeader() {
            // given
            UUID leaderId = UUID.randomUUID();

            // when
            Party party = Party.createParty(leaderId);

            // then
            assertThat(party).isNotNull();
            assertThat(party.getLeaderId()).isEqualTo(leaderId);
            assertThat(party.getMembers().size()).isEqualTo(1);
            assertThat(party.getMembers().contains(leaderId)).isTrue();
            assertThat(party.getStatus()).isEqualTo(PartyStatus.ACTIVE);
            assertThat(party.getLootDistribution()).isEqualTo(LootDistribution.FREE_FOR_ALL);
        }
    }

    @Nested
    @DisplayName("addMember 메소드는")
    class AddMemberTest {

        @Test
        @DisplayName("파티에 멤버를 추가한다")
        void addMember_ShouldAddMemberToParty() {
            // given
            UUID leaderId = UUID.randomUUID();
            UUID newMemberId = UUID.randomUUID();
            Party party = Party.createParty(leaderId);

            // when
            Party.AddPartyMemberResult result = party.addMember(newMemberId);

            // then
            assertThat(result).isEqualTo(Party.AddPartyMemberResult.SUCCESS);
            assertThat(party.getMembers().size()).isEqualTo(2);
            assertThat(party.getMembers().contains(newMemberId)).isTrue();
        }

        @Test
        @DisplayName("파티가 꽉 찼을 때는 멤버를 추가하지 않는다")
        void addMember_ShouldNotAddMemberWhenPartyIsFull() {
            // given
            UUID leaderId = UUID.randomUUID();
            UUID newMemberId = UUID.randomUUID();
            Party party = Party.createParty(leaderId);
            party.addMember(UUID.randomUUID());
            party.addMember(UUID.randomUUID());
            party.addMember(UUID.randomUUID());
            party.addMember(UUID.randomUUID());
            party.addMember(UUID.randomUUID());

            // when
            Party.AddPartyMemberResult result = party.addMember(newMemberId);

            // then
            assertThat(result).isEqualTo(Party.AddPartyMemberResult.PARTY_FULL);
            assertThat(party.getMembers().size()).isEqualTo(6);
            assertThat(party.getMembers().contains(newMemberId)).isFalse();
        }

        @Test
        @DisplayName("이미 파티에 있는 멤버는 다시 추가되지 않는다")
        void addMember_ShouldNotAddExistingMember() {
            // given
            UUID leaderId = UUID.randomUUID();
            Party party = Party.createParty(leaderId);

            // when
            Party.AddPartyMemberResult result = party.addMember(leaderId); // 이미 존재하는 leaderId를 다시 추가

            // then
            assertThat(result).isEqualTo(Party.AddPartyMemberResult.ALREADY_IN_SAME_PARTY);
            assertThat(party.getMembers().size()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("removeMember 메소드는")
    class RemoveMemberTest {

        @Test
        @DisplayName("파티에서 멤버를 제거한다")
        void removeMember_ShouldRemoveMemberFromParty() {
            // given
            UUID leaderId = UUID.randomUUID();
            UUID memberId = UUID.randomUUID();
            Party party = Party.createParty(leaderId);
            party.addMember(memberId);

            // when
            boolean result = party.removeMember(memberId);

            // then
            assertThat(result).isTrue();
            assertThat(party.getMembers().size()).isEqualTo(1);
            assertThat(party.getMembers().contains(memberId)).isFalse();
            assertThat(party.getLeaderId()).isEqualTo(leaderId); // 리더는 변경되지 않음
        }

        @Test
        @DisplayName("리더가 나갈 경우 가장 오래된 멤버가 리더가 된다")
        void removeMember_ShouldAssignNewLeaderWhenLeaderLeaves() {
            // given
            UUID leaderId = UUID.randomUUID();
            UUID member1Id = UUID.randomUUID();
            UUID member2Id = UUID.randomUUID();
            Party party = Party.createParty(leaderId);
            party.addMember(member1Id);
            party.addMember(member2Id);

            // when
            boolean result = party.removeMember(leaderId);

            // then
            assertThat(result).isTrue();
            assertThat(party.getMembers().size()).isEqualTo(2);
            assertThat(party.getMembers().contains(leaderId)).isFalse();
            assertThat(party.getLeaderId()).isEqualTo(member1Id); // 가장 오래된 멤버가 리더가 됨
        }

        @Test
        @DisplayName("모든 멤버가 나갈 경우 파티가 비활성화된다")
        void removeMember_ShouldDeactivatePartyWhenEmpty() {
            // given
            UUID leaderId = UUID.randomUUID();
            Party party = Party.createParty(leaderId);

            // when
            boolean result = party.removeMember(leaderId);

            // then
            assertThat(result).isTrue();
            assertThat(party.getMembers().isEmpty()).isTrue();
            assertThat(party.getStatus()).isEqualTo(PartyStatus.INACTIVE);
        }

        @Test
        @DisplayName("파티에 없는 멤버를 제거하려고 하면 false를 반환한다")
        void removeMember_ShouldReturnFalseForNonExistentMember() {
            // given
            UUID leaderId = UUID.randomUUID();
            UUID nonExistentMemberId = UUID.randomUUID();
            Party party = Party.createParty(leaderId);

            // when
            boolean result = party.removeMember(nonExistentMemberId);

            // then
            assertThat(result).isFalse();
            assertThat(party.getMembers().size()).isEqualTo(1);
            assertThat(party.getStatus()).isEqualTo(PartyStatus.ACTIVE);
        }
    }

    @Nested
    @DisplayName("changeLeader 메소드는")
    class ChangeLeaderTest {

        @Test
        @DisplayName("파티 리더를 변경한다")
        void changeLeader_ShouldChangePartyLeader() {
            // given
            UUID originalLeaderId = UUID.randomUUID();
            UUID newLeaderId = UUID.randomUUID();
            Party party = Party.createParty(originalLeaderId);
            party.addMember(newLeaderId);

            // when
            boolean result = party.changeLeader(newLeaderId);

            // then
            assertThat(result).isTrue();
            assertThat(party.getLeaderId()).isEqualTo(newLeaderId);
        }

        @Test
        @DisplayName("파티 멤버가 아닌 사람으로 리더를 변경하려고 하면 false를 반환한다")
        void changeLeader_ShouldReturnFalseForNonMember() {
            // given
            UUID leaderId = UUID.randomUUID();
            UUID nonMemberId = UUID.randomUUID();
            Party party = Party.createParty(leaderId);

            // when
            boolean result = party.changeLeader(nonMemberId);

            // then
            assertThat(result).isFalse();
            assertThat(party.getLeaderId()).isEqualTo(leaderId); // 리더 변경 없음
        }
    }
}
