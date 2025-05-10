package com.jefflife.mudmk2.game.application.service.model.request;

import lombok.Getter;

@Getter
public class UpdateAreaRequest {
    private final String name;

    public UpdateAreaRequest(final String name) {
        this.name = name;
    }
}
