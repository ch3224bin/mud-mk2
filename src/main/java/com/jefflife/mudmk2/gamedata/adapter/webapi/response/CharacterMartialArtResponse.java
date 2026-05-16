package com.jefflife.mudmk2.gamedata.adapter.webapi.response;

import com.jefflife.mudmk2.gamedata.application.domain.model.item.WeaponType;
import com.jefflife.mudmk2.gamedata.application.domain.model.martialart.*;
import com.jefflife.mudmk2.gamedata.application.service.provided.LearnedMartialArtFinder.CharacterMartialArtView;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

public record CharacterMartialArtResponse(
        UUID playerCharacterId,
        List<LearnedMentalView> learnedMentalMethods,
        List<LearnedExternalView> learnedExternalArts,
        Map<MentalMethodKind, UUID> equippedMentalSlots,
        List<UUID> equippedExternalSlots
) {
    public record LearnedMentalView(UUID id, Long templateId, String templateName,
                                    MentalMethodKind kind, int currentLevel, long currentExp) {}
    public record LearnedExternalView(UUID id, Long templateId, String templateName,
                                      WeaponType weaponType, int currentLevel, long currentExp) {}

    public static CharacterMartialArtResponse from(
            CharacterMartialArtView view,
            Function<Long, MentalMethodTemplate> mentalTpl,
            Function<Long, ExternalArtTemplate> externalTpl
    ) {
        List<LearnedMentalView> ms = view.learnedMentalMethods().stream().map(l -> {
            MentalMethodTemplate t = mentalTpl.apply(l.getMentalMethodTemplateId());
            return new LearnedMentalView(l.getId(), t.getId(), t.getName(),
                    t.getKind(), l.getCurrentLevel(), l.getCurrentExp());
        }).toList();
        List<LearnedExternalView> es = view.learnedExternalArts().stream().map(l -> {
            ExternalArtTemplate t = externalTpl.apply(l.getExternalArtTemplateId());
            return new LearnedExternalView(l.getId(), t.getId(), t.getName(),
                    t.getWeaponType(), l.getCurrentLevel(), l.getCurrentExp());
        }).toList();
        return new CharacterMartialArtResponse(
                view.playerCharacterId(), ms, es,
                view.equipped().getMentalSlots(),
                view.equipped().getExternalSlots());
    }
}
