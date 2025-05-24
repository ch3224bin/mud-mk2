package com.jefflife.mudmk2.gameplay.application.domain.model.combat;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.Combatable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;

class CombatGroupTest {

    private CombatGroup combatGroup;

    @Mock
    private Combatable combatable1;

    @Mock
    private Combatable combatable2;

    @Mock
    private Combatable combatable3;

    @BeforeEach
    void setUp() {
        combatGroup = new CombatGroup(CombatGroupType.ALLY);
    }

    @Test
    @DisplayName("getTarget은 패배하지 않은 참가자 중 어그로 점수가 가장 높은 대상을 반환한다")
    void getTarget_ShouldReturnParticipantWithHighestAggroScore() {
        // given
        CombatParticipant participant1 = new CombatParticipant(combatable1, 100, false);
        CombatParticipant participant2 = new CombatParticipant(combatable2, 50, false);
        CombatParticipant participant3 = new CombatParticipant(combatable3, 200, false);

        combatGroup.getParticipants().add(participant1);
        combatGroup.getParticipants().add(participant2);
        combatGroup.getParticipants().add(participant3);

        // when
        CombatParticipant target = combatGroup.getTarget();

        // then
        assertThat(target).isEqualTo(participant3);
    }

    @Test
    @DisplayName("패배한 참가자는 타겟으로 선택되지 않는다")
    void getTarget_ShouldNotReturnDefeatedParticipant() {
        // given
        CombatParticipant participant1 = new CombatParticipant(combatable1, 100, false);
        CombatParticipant participant2 = new CombatParticipant(combatable2, 50, false);
        CombatParticipant participant3 = new CombatParticipant(combatable3, 200, true);

        combatGroup.getParticipants().add(participant1);
        combatGroup.getParticipants().add(participant2);
        combatGroup.getParticipants().add(participant3);

        // when
        CombatParticipant target = combatGroup.getTarget();

        // then
        assertThat(target).isEqualTo(participant1); // 패배하지 않은 참가자 중 가장 높은 어그로 점수
    }

    @Test
    @DisplayName("모든 참가자가 패배했을 경우 null을 반환한다")
    void getTarget_ShouldReturnNull_WhenAllParticipantsAreDefeated() {
        // given
        CombatParticipant participant1 = new CombatParticipant(combatable1, 10, true);
        CombatParticipant participant2 = new CombatParticipant(combatable2, 100, true);

        combatGroup.getParticipants().add(participant1);
        combatGroup.getParticipants().add(participant2);

        // when
        CombatParticipant target = combatGroup.getTarget();

        // then
        assertThat(target).isNull();
    }

    @Test
    @DisplayName("참가자가 없을 경우 null을 반환한다")
    void getTarget_ShouldReturnNull_WhenNoParticipants() {
        // when
        CombatParticipant target = combatGroup.getTarget();

        // then
        assertThat(target).isNull();
    }
}
