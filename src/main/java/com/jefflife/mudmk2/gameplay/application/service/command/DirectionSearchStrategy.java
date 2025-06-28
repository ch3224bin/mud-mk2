package com.jefflife.mudmk2.gameplay.application.service.command;

import com.jefflife.mudmk2.gameplay.application.domain.model.look.DirectionLookable;
import com.jefflife.mudmk2.gameplay.application.domain.model.look.Lookable;
import com.jefflife.mudmk2.gamedata.application.domain.model.map.Direction;
import com.jefflife.mudmk2.gamedata.application.domain.model.map.Room;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.PlayerCharacter;
import com.jefflife.mudmk2.gameplay.application.domain.model.look.TargetSearchStrategy;
import com.jefflife.mudmk2.gameplay.application.service.GameWorldService;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class DirectionSearchStrategy implements TargetSearchStrategy {
    private final GameWorldService gameWorldService;
    
    public DirectionSearchStrategy(GameWorldService gameWorldService) {
        this.gameWorldService = gameWorldService;
    }
    
    @Override
    public Optional<Lookable> search(Long userId, String targetName) {
        if (!Direction.contains(targetName)) {
            return Optional.empty();
        }

        PlayerCharacter player = gameWorldService.getPlayerByUserId(userId);
        Room currentRoom = gameWorldService.getRoom(player.getCurrentRoomId());

        Direction direction = Direction.valueOfString(targetName);
        Optional<Room> targetRoom = currentRoom.getNextRoomByDirection(direction);
        
        return targetRoom.map(room -> new DirectionLookable(direction, room));
    }
    
    @Override
    public int getPriority() {
        return 1; // 가장 높은 우선순위
    }
}