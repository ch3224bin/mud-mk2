package com.jefflife.mudmk2.gameplay.application.domain.model.combat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CombatActionResult {
    static final CombatActionResult NOT_ACTED_RESULT = new CombatActionResult();
    private List<CombatLog> logs = Collections.emptyList();
    private boolean acted = false;

    public void addLogs(List<CombatLog> combatLogs) {
        if (logs.isEmpty()) {
            logs = new ArrayList<>();
        }
        logs.addAll(combatLogs);
        acted = !logs.isEmpty();
    }

    /**
     * Returns the combat logs recorded during this action.
     * Used primarily for testing.
     *
     * @return The list of combat logs
     */
    public List<CombatLog> getLogs() {
        return logs;
    }

    /**
     * Returns whether any action was taken during this turn.
     *
     * @return true if action was taken, false otherwise
     */
    public boolean isActed() {
        return acted;
    }
}
