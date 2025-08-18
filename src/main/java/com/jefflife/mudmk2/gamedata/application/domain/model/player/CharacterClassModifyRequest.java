package com.jefflife.mudmk2.gamedata.application.domain.model.player;

import jakarta.validation.constraints.Min;

/**
 * 캐릭터 직업 수정 요청 DTO
 */
public record CharacterClassModifyRequest(
    String name,

    String code,

    String description,

    @Min(value = 1, message = "기본 HP는 최소 1 이상이어야 합니다")
    Integer baseHp,

    @Min(value = 0, message = "기본 MP는 최소 0 이상이어야 합니다")
    Integer baseMp,

    @Min(value = 1, message = "기본 STR은 최소 1 이상이어야 합니다")
    Integer baseStr,

    @Min(value = 1, message = "기본 DEX는 최소 1 이상이어야 합니다")
    Integer baseDex,

    @Min(value = 1, message = "기본 CON은 최소 1 이상이어야 합니다")
    Integer baseCon,

    @Min(value = 1, message = "기본 INT는 최소 1 이상이어야 합니다")
    Integer baseIntelligence,

    @Min(value = 1, message = "기본 POW는 최소 1 이상이어야 합니다")
    Integer basePow,

    @Min(value = 1, message = "기본 CHA는 최소 1 이상이어야 합니다")
    Integer baseCha
) {}