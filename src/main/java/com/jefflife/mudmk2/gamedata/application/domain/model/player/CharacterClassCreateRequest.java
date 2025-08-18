package com.jefflife.mudmk2.gamedata.application.domain.model.player;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 캐릭터 직업 생성 요청 DTO
 */
public record CharacterClassCreateRequest(
    @NotBlank(message = "직업 코드는 필수입니다")
    String code,

    @NotBlank(message = "직업 이름은 필수입니다")
    String name,

    String description,

    @NotNull(message = "기본 HP는 필수입니다")
    @Min(value = 1, message = "기본 HP는 최소 1 이상이어야 합니다")
    Integer baseHp,

    @NotNull(message = "기본 MP는 필수입니다")
    @Min(value = 0, message = "기본 MP는 최소 0 이상이어야 합니다")
    Integer baseMp,

    @NotNull(message = "기본 STR은 필수입니다")
    @Min(value = 1, message = "기본 STR은 최소 1 이상이어야 합니다")
    Integer baseStr,

    @NotNull(message = "기본 DEX는 필수입니다")
    @Min(value = 1, message = "기본 DEX는 최소 1 이상이어야 합니다")
    Integer baseDex,

    @NotNull(message = "기본 CON은 필수입니다")
    @Min(value = 1, message = "기본 CON은 최소 1 이상이어야 합니다")
    Integer baseCon,

    @NotNull(message = "기본 INT는 필수입니다")
    @Min(value = 1, message = "기본 INT는 최소 1 이상이어야 합니다")
    Integer baseIntelligence,

    @NotNull(message = "기본 POW는 필수입니다")
    @Min(value = 1, message = "기본 POW는 최소 1 이상이어야 합니다")
    Integer basePow,

    @NotNull(message = "기본 CHA는 필수입니다")
    @Min(value = 1, message = "기본 CHA는 최소 1 이상이어야 합니다")
    Integer baseCha
) {}