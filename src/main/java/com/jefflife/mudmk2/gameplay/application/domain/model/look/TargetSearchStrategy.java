package com.jefflife.mudmk2.gameplay.application.domain.model.look;

import java.util.Optional;

/**
 * 대상 검색 전략을 정의하는 인터페이스입니다.
 * 다양한 검색 로직을 구현하여 특정 사용자와 이름에 해당하는 Lookable 객체를 찾을 수 있습니다.
 * 또한, 전략의 우선순위를 정의하여 여러 전략 중에서 선택적으로 사용할 수 있도록 지원합니다.
 */
public interface TargetSearchStrategy {
    /**
     * 지정된 사용자 ID와 대상 이름에 기반하여 Lookable 객체를 검색합니다.
     *
     * @param userId 검색 대상이 속한 사용자 ID
     * @param targetName 검색할 대상의 이름
     * @return 검색된 Lookable 객체를 포함하는 Optional 객체. 대상이 없을 경우 비어 있는 Optional을 반환
     */
    Optional<Lookable> search(Long userId, String targetName);

    /**
     * 검색 전략의 우선순위를 반환합니다.
     * 우선순위가 낮은 값일수록 해당 전략이 더 먼저 적용됩니다.
     *
     * @return 해당 검색 전략의 우선순위 값
     */
    int getPriority();
}
