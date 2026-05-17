package com.jefflife.mudmk2.gamedata.application.domain.model.martialart;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "mental_method_template")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MentalMethodTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MentalMethodKind kind;

    @Column(nullable = false)
    private int maxLevel;

    @Convert(converter = MentalMethodLevelEffectsConverter.class)
    @Column(columnDefinition = "TEXT", nullable = false)
    private List<MentalMethodLevelEffect> levelEffects;

    private MentalMethodTemplate(String name, String description, MentalMethodKind kind,
                                 int maxLevel, List<MentalMethodLevelEffect> levelEffects) {
        validate(maxLevel, levelEffects);
        this.name = name;
        this.description = description;
        this.kind = kind;
        this.maxLevel = maxLevel;
        this.levelEffects = List.copyOf(levelEffects);
    }

    public static Builder builder() { return new Builder(); }

    public void update(String name, String description, MentalMethodKind kind,
                       int maxLevel, List<MentalMethodLevelEffect> levelEffects) {
        validate(maxLevel, levelEffects);
        this.name = name;
        this.description = description;
        this.kind = kind;
        this.maxLevel = maxLevel;
        this.levelEffects = List.copyOf(levelEffects);
    }

    private static void validate(int maxLevel, List<MentalMethodLevelEffect> levelEffects) {
        if (maxLevel < 1) {
            throw new IllegalArgumentException("maxLevel must be >= 1: " + maxLevel);
        }
        if (levelEffects == null || levelEffects.size() != maxLevel) {
            throw new IllegalArgumentException(
                    "levelEffects.size must equal maxLevel: expected " + maxLevel
                            + ", actual " + (levelEffects == null ? 0 : levelEffects.size()));
        }
        for (int i = 0; i < levelEffects.size(); i++) {
            int expected = i + 1;
            if (levelEffects.get(i).level() != expected) {
                throw new IllegalArgumentException(
                        "levelEffects must be ordered 1..maxLevel; index " + i
                                + " expected level " + expected
                                + ", got " + levelEffects.get(i).level());
            }
        }
    }

    public MentalMethodLevelEffect effectAt(int level) {
        if (level < 1 || level > maxLevel) {
            throw new IllegalArgumentException(
                    "level must be in 1.." + maxLevel + ", got " + level);
        }
        return levelEffects.get(level - 1);
    }

    public static class Builder {
        private String name;
        private String description;
        private MentalMethodKind kind;
        private int maxLevel;
        private List<MentalMethodLevelEffect> levelEffects;

        public Builder name(String v) { this.name = v; return this; }
        public Builder description(String v) { this.description = v; return this; }
        public Builder kind(MentalMethodKind v) { this.kind = v; return this; }
        public Builder maxLevel(int v) { this.maxLevel = v; return this; }
        public Builder levelEffects(List<MentalMethodLevelEffect> v) { this.levelEffects = v; return this; }

        public MentalMethodTemplate build() {
            return new MentalMethodTemplate(name, description, kind, maxLevel, levelEffects);
        }
    }
}
