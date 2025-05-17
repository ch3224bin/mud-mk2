package com.jefflife.mudmk2.gameplay.application.tick;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class TickEngine {

    private final List<TickListener> listeners;
    private long tickCount = 0;

    public TickEngine(final List<TickListener> listeners) {
        this.listeners = listeners;
    }

    @Scheduled(fixedDelayString = "${game.tick.interval:1000}") // 1초마다 tick
    public void tick() {
        tickCount++;
        log.debug("Game tick: {}", tickCount);
        
        // 등록된 모든 리스너에게 tick 이벤트 알림
        for (TickListener listener : listeners) {
            try {
                listener.onTick(tickCount);
            } catch (Exception e) {
                log.error("Error processing tick for listener: {}", listener.getClass().getSimpleName(), e);
            }
        }
    }
    
    public void registerListener(TickListener listener) {
        listeners.add(listener);
    }
    
    public void removeListener(TickListener listener) {
        listeners.remove(listener);
    }
    
    public long getCurrentTick() {
        return tickCount;
    }
}