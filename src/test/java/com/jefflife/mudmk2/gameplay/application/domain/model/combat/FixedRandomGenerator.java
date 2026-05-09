package com.jefflife.mudmk2.gameplay.application.domain.model.combat;

public class FixedRandomGenerator implements RandomGenerator {
    private final int fixedValue;

    public FixedRandomGenerator(int fixedValue) {
        this.fixedValue = fixedValue;
    }

    @Override
    public int nextInt(int bound) {
        return Math.min(fixedValue, bound - 1);
    }
}
