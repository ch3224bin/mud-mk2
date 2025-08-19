package com.jefflife.mudmk2.gamedata.adapter.webapi.response;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.CharacterClassEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 캐릭터 직업 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CharacterClassResponse {
    private Long id;
    private String code;
    private String name;
    private String description;
    private int baseHp;
    private int baseMp;
    private int baseStr;
    private int baseDex;
    private int baseCon;
    private int baseIntelligence;
    private int basePow;
    private int baseCha;

    /**
     * CharacterClassEntity를 CharacterClassResponse로 변환합니다.
     *
     * @param entity 변환할 엔티티
     * @return 변환된 응답 DTO
     */
    public static CharacterClassResponse fromEntity(CharacterClassEntity entity) {
        return CharacterClassResponse.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .name(entity.getName())
                .description(entity.getDescription())
                .baseHp(entity.getBaseHp())
                .baseMp(entity.getBaseMp())
                .baseStr(entity.getBaseStr())
                .baseDex(entity.getBaseDex())
                .baseCon(entity.getBaseCon())
                .baseIntelligence(entity.getBaseIntelligence())
                .basePow(entity.getBasePow())
                .baseCha(entity.getBaseCha())
                .build();
    }

    public static List<CharacterClassResponse> fromEntities(List<CharacterClassEntity> entities) {
        return entities.stream()
                .map(CharacterClassResponse::fromEntity)
                .collect(Collectors.toList());
    }
}
