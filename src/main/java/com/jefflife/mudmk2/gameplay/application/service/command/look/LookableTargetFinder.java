package com.jefflife.mudmk2.gameplay.application.service.command.look;

import java.util.Optional;

/**
 * LookableTargetFinder는 지정된 사용자 ID와 대상 이름을 기반으로
 * 방 안에서 대상(Lookable)을 검색하는 기능을 제공하는 인터페이스입니다.
 * 이 인터페이스를 구현함으로써 게임 내의 룸에서 특정 대상을 찾는
 * 필터링 로직을 구현할 수 있습니다.
 */
public interface LookableTargetFinder {
    /**
     * 지정된 사용자와 같은 방 안에서 대상 객체(Lookable)를 검색합니다.
     * 검색 결과는 Optional로 반환되며, 대상이 검색되지 않을 경우 빈 Optional이 반환됩니다.
     *
     * @param userId 검색 대상이 속한 사용자 ID
     * @param targetName 검색할 대상의 이름
     * @return 검색된 Lookable 객체를 포함하는 Optional 객체. 대상이 없을 경우 비어 있는 Optional을 반환
     */
    Optional<Lookable> findTargetInRoom(Long userId, String targetName);
}