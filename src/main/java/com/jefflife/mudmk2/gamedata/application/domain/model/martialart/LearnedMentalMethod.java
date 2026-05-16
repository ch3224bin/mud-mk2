package com.jefflife.mudmk2.gamedata.application.domain.model.martialart;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "learned_mental_method",
       uniqueConstraints = @UniqueConstraint(
               name = "uq_learned_mental_pc_template",
               columnNames = {"player_character_id", "mental_method_template_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LearnedMentalMethod {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "player_character_id", nullable = false)
    private UUID playerCharacterId;

    @Column(name = "mental_method_template_id", nullable = false)
    private Long mentalMethodTemplateId;

    @Column(nullable = false)
    private int currentLevel;

    @Column(nullable = false)
    private long currentExp;

    private LearnedMentalMethod(UUID playerCharacterId, Long mentalMethodTemplateId) {
        this.playerCharacterId = playerCharacterId;
        this.mentalMethodTemplateId = mentalMethodTemplateId;
        this.currentLevel = 1;
        this.currentExp = 0L;
    }

    public static LearnedMentalMethod create(UUID playerCharacterId, Long mentalMethodTemplateId) {
        return new LearnedMentalMethod(playerCharacterId, mentalMethodTemplateId);
    }
}
