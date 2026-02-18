package com.jefflife.mudmk2.gameplay.application.service.provided;

import com.jefflife.mudmk2.gameplay.application.domain.model.command.RecruitCommand;

public interface RecruitUseCase {
    void recruit(RecruitCommand recruitCommand);
}
