package com.jefflife.mudmk2.gameplay.application.domain.model.command;

import org.apache.commons.lang3.StringUtils;

/**
 * Command for speaking or saying something in the game.
 * Example: "안녕하세요 말" (Say hello to everyone in the room)
 * Example: "<target> 안녕하세요 말" (Say hello to a specific target)
 */
public record SpeakCommand(
    Long userId,
    String target,
    String message
) implements Command {
    public boolean hasTarget() {
        return StringUtils.isNotBlank(target);
    }
}
