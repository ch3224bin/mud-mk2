package com.jefflife.mudmk2.gameplay.application.domain.model.combat;

import java.util.Random;

/**
 * java.util.Random을 사용하는 RandomGenerator 기본 구현체
 */
public class DefaultRandomGenerator implements RandomGenerator {
    private final Random random;

    public DefaultRandomGenerator() {
        this.random = new Random();
    }

    @Override
    public int nextInt(int bound) {
        return random.nextInt(bound);
    }
}
