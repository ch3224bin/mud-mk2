package com.jefflife.mudmk2.gamedata.application.domain.model.map;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AreaModifyRequest(@NotBlank @Size(min = 1, max = 255) String name) {
}
