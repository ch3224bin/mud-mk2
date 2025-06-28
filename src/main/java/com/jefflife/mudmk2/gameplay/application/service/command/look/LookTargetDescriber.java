package com.jefflife.mudmk2.gameplay.application.service.command.look;

public interface LookTargetDescriber {
    void describe(Long userId, Lookable target);
    LookableType getLookableType();
}