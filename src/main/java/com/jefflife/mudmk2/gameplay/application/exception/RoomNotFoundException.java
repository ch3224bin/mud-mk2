package com.jefflife.mudmk2.gameplay.application.exception;

public class RoomNotFoundException extends RuntimeException {
    public RoomNotFoundException(Long roomId) {
        super("Room not found: " + roomId);
    }
}
