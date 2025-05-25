package com.jefflife.mudmk2.gameplay.application.domain.model.combat;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.CharacterStats;

@FunctionalInterface
public interface InitiativeProvider {
    /**
     * 캐릭터의 스탯을 기반으로 선제권을 계산합니다.
     * @param stats 캐릭터의 스탯
     * @return 선제권 굴림 결과
     */
    InitiativeRoll calculate(CharacterStats stats);
}