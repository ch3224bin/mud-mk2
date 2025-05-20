package com.jefflife.mudmk2.gamedata.application.service.exception;

/**
 * 이미 존재하는 캐릭터 직업 코드로 생성/수정을 시도할 때 발생하는 예외
 */
public class DuplicateCharacterClassException extends RuntimeException {
    public DuplicateCharacterClassException(String message) {
        super(message);
    }
}
