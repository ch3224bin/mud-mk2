package com.jefflife.mudmk2.gameplay.application.service;

import com.jefflife.mudmk2.gamedata.application.domain.model.item.ItemInstance;
import com.jefflife.mudmk2.gamedata.application.domain.model.item.ItemTemplate;
import com.jefflife.mudmk2.gamedata.application.domain.model.map.Room;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.PlayerCharacter;
import com.jefflife.mudmk2.gamedata.application.service.model.request.ItemInstancePlaceRequest;
import com.jefflife.mudmk2.gamedata.application.service.model.request.ItemInstancePlaceRequest.LocationType;
import com.jefflife.mudmk2.gamedata.application.service.provided.ItemInstancePlacer;
import com.jefflife.mudmk2.gamedata.application.service.required.ItemInstanceRepository;
import com.jefflife.mudmk2.gamedata.application.service.required.ItemTemplateRepository;
import com.jefflife.mudmk2.gamedata.application.service.required.PlayerCharacterRepository;
import com.jefflife.mudmk2.gamedata.application.service.required.RoomRepository;
import com.jefflife.mudmk2.gameplay.application.service.required.ActiveRoomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@Transactional
public class ItemInstanceService implements ItemInstancePlacer {

    private final ItemTemplateRepository itemTemplateRepository;
    private final ItemInstanceRepository itemInstanceRepository;
    private final RoomRepository roomRepository;
    private final PlayerCharacterRepository playerCharacterRepository;
    private final GameWorldService gameWorldService;
    private final ActiveRoomRepository rooms;

    public ItemInstanceService(
        ItemTemplateRepository itemTemplateRepository,
        ItemInstanceRepository itemInstanceRepository,
        RoomRepository roomRepository,
        PlayerCharacterRepository playerCharacterRepository,
        GameWorldService gameWorldService,
        ActiveRoomRepository rooms
    ) {
        this.itemTemplateRepository = itemTemplateRepository;
        this.itemInstanceRepository = itemInstanceRepository;
        this.roomRepository = roomRepository;
        this.playerCharacterRepository = playerCharacterRepository;
        this.gameWorldService = gameWorldService;
        this.rooms = rooms;
    }

    @Override
    public ItemInstance place(ItemInstancePlaceRequest request) {
        ItemTemplate template = itemTemplateRepository.findById(request.templateId())
            .orElseThrow(() -> new NoSuchElementException("ItemTemplate not found: " + request.templateId()));

        ItemInstance instance = itemInstanceRepository.save(new ItemInstance(template, request.quantity()));

        if (request.locationType() == LocationType.ROOM) {
            long roomId;
            try {
                roomId = Long.parseLong(request.locationId());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid room ID: " + request.locationId());
            }
            placeInRoom(instance, roomId);
        } else {
            UUID characterId;
            try {
                characterId = UUID.fromString(request.locationId());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid character ID: " + request.locationId());
            }
            placeInCharacter(instance, characterId);
        }

        return instance;
    }

    private void placeInRoom(ItemInstance instance, Long roomId) {
        Room room = roomRepository.findById(roomId)
            .orElseThrow(() -> new NoSuchElementException("Room not found: " + roomId));
        room.addFloorItem(instance);
        roomRepository.save(room);
        rooms.findById(roomId).ifPresent(r -> {
            if (r != room) {
                r.addFloorItem(instance);
            }
        });
    }

    private void placeInCharacter(ItemInstance instance, UUID characterId) {
        PlayerCharacter character = playerCharacterRepository.findById(characterId)
            .orElseThrow(() -> new NoSuchElementException("PlayerCharacter not found: " + characterId));
        character.getInventory().addItem(instance);
        playerCharacterRepository.save(character);
        gameWorldService.getPlayerById(characterId).ifPresent(p -> {
            if (p != character) {
                p.getInventory().addItem(instance);
            }
        });
    }
}
