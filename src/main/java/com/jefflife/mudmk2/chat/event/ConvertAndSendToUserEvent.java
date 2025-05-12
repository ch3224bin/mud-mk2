package com.jefflife.mudmk2.chat.event;

public record ConvertAndSendToUserEvent(
        String username,
        String message
) {

}
