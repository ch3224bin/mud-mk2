package com.jefflife.mudmk2.gameplay.application.service.command.look;

import java.util.Map;

/**
 * Lookable 인터페이스는 객체의 이름과 속성을 조회할 수 있는 기능을 정의합니다.
 * 주로 게임 내에서 특정 대상의 정보를 참조하거나 처리하는 데 사용됩니다.
 */
public interface Lookable {
    /**
     * 대상의 이름을 반환합니다.
     *
     * @return 대상의 이름을 나타내는 문자열
     */
    String getName();

    /**
     * 대상의 유형(LookableType)을 반환합니다.
     * Lookable 인터페이스를 구현한 객체의 구체적인 유형을 나타냅니다.
     *
     * @return 대상의 유형을 나타내는 LookableType 열거형 값
     */
    LookableType getType();

    /**
     * 대상의 속성 정보를 조회합니다.
     * 반환된 속성 정보는 키-값 쌍으로 구성된 맵 형태이며, 각 키는 속성의 이름,
     * 값은 해당 속성의 구체적인 정보를 나타냅니다.
     *
     * @return 대상의 속성 정보를 포함하는 {@code Map<String, Object>} 객체
     */
    Map<String, Object> getProperties();
}
