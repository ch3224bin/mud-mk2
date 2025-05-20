package com.jefflife.mudmk2.gamedata.application.service.exception;

/**
 * 존재하지 않는 캐릭터 직업을 요청했을 때 발생하는 예외
 */
public class CharacterClassNotFoundException extends RuntimeException {
    public CharacterClassNotFoundException(String message) {
        super(message);
    }
}
