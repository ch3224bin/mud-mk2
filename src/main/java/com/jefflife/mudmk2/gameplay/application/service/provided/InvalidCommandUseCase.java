package com.jefflife.mudmk2.gameplay.application.service.provided;

public interface InvalidCommandUseCase {
    /**
     * 잘못된 명령어 메시지를 사용자에게 전송합니다.
     *
     * @param userId 사용자 ID
     * @param originalCommand 원래 입력한 명령어
     */
    void notifyInvalidCommand(Long userId, String originalCommand);
}
