package com.jefflife.mudmk2.gameplay.application.service.model.template;

public record EatSuccessVariables(
        Long userId,
        String itemName,
        int hpRecovery,
        int mpRecovery,
        int apRecovery
) {
}
