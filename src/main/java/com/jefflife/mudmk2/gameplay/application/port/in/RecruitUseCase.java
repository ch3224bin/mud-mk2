package com.jefflife.mudmk2.gameplay.application.port.in;

import com.jefflife.mudmk2.gameplay.application.domain.model.command.RecruitCommand;

public interface RecruitUseCase {
    void recruit(RecruitCommand recruitCommand);
}
