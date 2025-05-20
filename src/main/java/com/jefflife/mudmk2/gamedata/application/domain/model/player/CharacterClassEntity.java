package com.jefflife.mudmk2.gamedata.application.domain.model.player;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 캐릭터 직업 엔티티
 * <p>
 * 게임 내 직업(Class)을 나타내는 엔티티입니다.
 * 기존 CharacterClass enum을 대체합니다.
 * </p>
 */
@Entity
@Table(name = "character_classes")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CharacterClassEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code; // WARRIOR, MAGE 등 기존 enum 값

    @Column(nullable = false)
    private String name; // 표시 이름 (전사, 마법사 등)

    private String description;

    // 기본 스탯 정보
    private int baseHp;
    private int baseMp;
    private int baseStr;
    private int baseDex;
    private int baseCon;
    private int baseIntelligence;
    private int basePow;
    private int baseCha;

    // 생성/수정 시간
    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    /**
     * 주어진 코드로 이 직업이 특정 기존 enum 값과 일치하는지 확인합니다.
     * 마이그레이션 중에 유용하게 사용될 수 있습니다.
     *
     * @param characterClass 확인할 enum 값
     * @return 일치하면 true, 그렇지 않으면 false
     */
    public boolean is(CharacterClass characterClass) {
        return this.code.equals(characterClass.name());
    }

    /**
     * 이 엔티티의 문자열 표현을 반환합니다.
     * 이름을 반환하여 기존 enum.toString()과 유사하게 작동하도록 합니다.
     *
     * @return 직업 이름
     */
    @Override
    public String toString() {
        return this.name;
    }
}
