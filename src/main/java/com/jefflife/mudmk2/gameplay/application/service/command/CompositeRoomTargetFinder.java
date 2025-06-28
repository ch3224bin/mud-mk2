package com.jefflife.mudmk2.gameplay.application.service.command;

import com.jefflife.mudmk2.gameplay.application.domain.model.look.Lookable;
import com.jefflife.mudmk2.gameplay.application.domain.model.look.LookableTargetFinder;
import com.jefflife.mudmk2.gameplay.application.domain.model.look.TargetSearchStrategy;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Component
public class CompositeRoomTargetFinder implements LookableTargetFinder {
    private final List<TargetSearchStrategy> searchStrategies;

   public CompositeRoomTargetFinder(List<TargetSearchStrategy> searchStrategies) {
        this.searchStrategies = searchStrategies.stream()
                .sorted(Comparator.comparing(TargetSearchStrategy::getPriority))
                .toList();
    }

    @Override
    public Optional<Lookable> findTargetInRoom(Long userId, String targetName) {
        return searchStrategies.stream()
                .map(strategy -> strategy.search(userId, targetName))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();
    }
}
