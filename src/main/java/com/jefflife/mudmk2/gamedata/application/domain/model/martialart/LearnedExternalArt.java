package com.jefflife.mudmk2.gamedata.application.domain.model.martialart;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "learned_external_art",
       uniqueConstraints = @UniqueConstraint(
               name = "uq_learned_external_pc_template",
               columnNames = {"player_character_id", "external_art_template_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LearnedExternalArt {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "player_character_id", nullable = false)
    private UUID playerCharacterId;

    @Column(name = "external_art_template_id", nullable = false)
    private Long externalArtTemplateId;

    @Column(nullable = false)
    private int currentLevel;

    @Column(nullable = false)
    private long currentExp;

    private LearnedExternalArt(UUID playerCharacterId, Long externalArtTemplateId) {
        this.playerCharacterId = playerCharacterId;
        this.externalArtTemplateId = externalArtTemplateId;
        this.currentLevel = 1;
        this.currentExp = 0L;
    }

    public static LearnedExternalArt create(UUID playerCharacterId, Long externalArtTemplateId) {
        return new LearnedExternalArt(playerCharacterId, externalArtTemplateId);
    }
}
