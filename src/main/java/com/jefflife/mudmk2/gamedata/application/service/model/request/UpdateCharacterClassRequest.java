package com.jefflife.mudmk2.gamedata.application.service.model.request;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 캐릭터 직업 수정 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateCharacterClassRequest {

    private String name;

    private String code;

    private String description;

    @Min(value = 1, message = "기본 HP는 최소 1 이상이어야 합니다")
    private Integer baseHp;

    @Min(value = 0, message = "기본 MP는 최소 0 이상이어야 합니다")
    private Integer baseMp;

    @Min(value = 1, message = "기본 STR은 최소 1 이상이어야 합니다")
    private Integer baseStr;

    @Min(value = 1, message = "기본 DEX는 최소 1 이상이어야 합니다")
    private Integer baseDex;

    @Min(value = 1, message = "기본 CON은 최소 1 이상이어야 합니다")
    private Integer baseCon;

    @Min(value = 1, message = "기본 INT는 최소 1 이상이어야 합니다")
    private Integer baseIntelligence;

    @Min(value = 1, message = "기본 POW는 최소 1 이상이어야 합니다")
    private Integer basePow;

    @Min(value = 1, message = "기본 CHA는 최소 1 이상이어야 합니다")
    private Integer baseCha;
}
