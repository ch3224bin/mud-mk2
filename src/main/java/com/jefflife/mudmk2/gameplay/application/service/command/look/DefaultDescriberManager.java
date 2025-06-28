package com.jefflife.mudmk2.gameplay.application.service.command.look;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

@Component
public class DefaultDescriberManager implements DescriberManager {
    private final Map<LookableType, LookTargetDescriber> describerMap;

    public DefaultDescriberManager(List<LookTargetDescriber> describerList) {
        describerMap = describerList.stream()
                .collect(Collectors.toMap(
                        LookTargetDescriber::getLookableType,
                        Function.identity()
                ));
    }

    @Override
    public void findAndExecute(Long userId, Lookable target) {
        LookTargetDescriber lookTargetDescriber = describerMap.get(target.getType());
        requireNonNull(lookTargetDescriber, "No describer found for target: " + target.getType());

        lookTargetDescriber.describe(userId, target);
    }
}