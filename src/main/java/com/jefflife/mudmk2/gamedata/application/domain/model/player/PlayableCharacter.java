package com.jefflife.mudmk2.gamedata.application.domain.model.player;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter @NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlayableCharacter {
    // 레벨과 경험치
    private int level = 1;
    private long experience = 0;
    private long nextLevelExp = 100;
    
    // 대화 가능 여부
    private boolean conversable = true;

    @Builder
    public PlayableCharacter(
            final int level,
            final long experience,
            final long nextLevelExp,
            final boolean conversable
    ) {
        this.level = level;
        this.experience = experience;
        this.nextLevelExp = nextLevelExp;
        this.conversable = conversable;
    }
}