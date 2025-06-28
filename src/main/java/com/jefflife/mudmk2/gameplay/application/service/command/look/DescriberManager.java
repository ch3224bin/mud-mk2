package com.jefflife.mudmk2.gameplay.application.service.command.look;

public interface DescriberManager {
    void findAndExecute(Long userId, Lookable target);
}
