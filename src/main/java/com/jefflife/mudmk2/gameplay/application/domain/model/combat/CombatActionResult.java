package com.jefflife.mudmk2.gameplay.application.domain.model.combat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CombatActionResult {

    public static final CombatActionResult NOT_ACTED = new ImmutableNotActed();

    private List<CombatLog> logs = Collections.emptyList();
    private boolean acted = false;

    public void addLog(CombatLog log) {
        if (logs.isEmpty()) logs = new ArrayList<>();
        logs.add(log);
        acted = true;
    }

    public void addLogs(List<CombatLog> newLogs) {
        newLogs.forEach(this::addLog);
    }

    public List<CombatLog> getLogs() { return logs; }
    public boolean isActed() { return acted; }

    private static final class ImmutableNotActed extends CombatActionResult {
        @Override
        public void addLog(CombatLog log) {
            throw new UnsupportedOperationException("NOT_ACTED sentinel is immutable");
        }

        @Override
        public void addLogs(List<CombatLog> newLogs) {
            throw new UnsupportedOperationException("NOT_ACTED sentinel is immutable");
        }
    }
}
