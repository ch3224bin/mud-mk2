package com.jefflife.mudmk2.gameplay.application.domain.model.command;

import java.util.List;

/**
 * Command for combining items in the game.
 * Example: "나뭇가지 기름 헝겊 조합" (Combine branch, oil, cloth)
 */
public record CombineCommand(
    String username,
    List<String> items
) implements Command {
}