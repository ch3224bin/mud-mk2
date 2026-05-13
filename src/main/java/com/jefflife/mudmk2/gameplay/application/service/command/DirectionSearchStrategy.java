package com.jefflife.mudmk2.gameplay.application.service.command;

import com.jefflife.mudmk2.gameplay.application.service.command.look.DirectionLookable;
import com.jefflife.mudmk2.gameplay.application.service.command.look.Lookable;
import com.jefflife.mudmk2.gamedata.application.domain.model.map.Direction;
import com.jefflife.mudmk2.gamedata.application.domain.model.map.Room;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.PlayerCharacter;
import com.jefflife.mudmk2.gameplay.application.exception.PlayerNotFoundException;
import com.jefflife.mudmk2.gameplay.application.exception.RoomNotFoundException;
import com.jefflife.mudmk2.gameplay.application.service.command.look.TargetSearchStrategy;
import com.jefflife.mudmk2.gameplay.application.service.required.ActivePlayerRepository;
import com.jefflife.mudmk2.gameplay.application.service.required.ActiveRoomRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class DirectionSearchStrategy implements TargetSearchStrategy {
    private final ActivePlayerRepository players;
    private final ActiveRoomRepository rooms;

    public DirectionSearchStrategy(ActivePlayerRepository players, ActiveRoomRepository rooms) {
        this.players = players;
        this.rooms = rooms;
    }

    @Override
    public Optional<Lookable> search(Long userId, String targetName, int index) {
        if (!Direction.contains(targetName)) {
            return Optional.empty();
        }

        PlayerCharacter player = players.findByUserId(userId)
                .orElseThrow(() -> new PlayerNotFoundException(userId));
        Long currentRoomId = player.getCurrentRoomId();
        Room currentRoom = rooms.findById(currentRoomId)
                .orElseThrow(() -> new RoomNotFoundException(currentRoomId));

        Direction direction = Direction.valueOfString(targetName);
        Optional<Room> targetRoom = currentRoom.getNextRoomByDirection(direction);
        
        return targetRoom.map(room -> new DirectionLookable(direction, room));
    }
    
    @Override
    public int getPriority() {
        return 1; // 가장 높은 우선순위
    }
}