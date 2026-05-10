package com.jefflife.mudmk2.gamedata.application.domain.model.item;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class StatModifier {

    @Enumerated(EnumType.STRING)
    private StatType statType;

    private int value;
}
