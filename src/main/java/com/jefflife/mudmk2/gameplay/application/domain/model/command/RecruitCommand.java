package com.jefflife.mudmk2.gameplay.application.domain.model.command;

public record RecruitCommand(
        Long userId,
        String npcName
) implements Command {
}
