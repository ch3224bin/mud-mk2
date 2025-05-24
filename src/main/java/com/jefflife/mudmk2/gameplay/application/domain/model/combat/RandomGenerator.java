package com.jefflife.mudmk2.gameplay.application.domain.model.combat;

public interface RandomGenerator {
    /**
     * 0부터 bound-1 사이의 랜덤 값을 반환합니다.
     * @param bound 상한값 (이 값은 포함되지 않음)
     * @return 0부터 bound-1 사이의 랜덤 값
     */
    int nextInt(int bound);
}
