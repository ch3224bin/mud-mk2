package com.jefflife.mudmk2.gamedata.adapter.webapi.response;

import com.jefflife.mudmk2.gamedata.application.domain.model.item.StatModifier;
import com.jefflife.mudmk2.gamedata.application.domain.model.item.StatType;

public record StatModifierResponse(StatType statType, int value) {
    public static StatModifierResponse from(StatModifier modifier) {
        return new StatModifierResponse(modifier.getStatType(), modifier.getValue());
    }
}
