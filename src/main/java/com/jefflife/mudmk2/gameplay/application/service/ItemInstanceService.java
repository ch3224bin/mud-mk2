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

    public ItemInstanceService(
        ItemTemplateRepository itemTemplateRepository,
        ItemInstanceRepository itemInstanceRepository,
        RoomRepository roomRepository,
        PlayerCharacterRepository playerCharacterRepository,
        GameWorldService gameWorldService
    ) {
        this.itemTemplateRepository = itemTemplateRepository;
        this.itemInstanceRepository = itemInstanceRepository;
        this.roomRepository = roomRepository;
        this.playerCharacterRepository = playerCharacterRepository;
        this.gameWorldService = gameWorldService;
    }

    @Override
    public ItemInstance place(ItemInstancePlaceRequest request) {
        ItemTemplate template = itemTemplateRepository.findById(request.templateId())
            .orElseThrow(() -> new NoSuchElementException("ItemTemplate not found: " + request.templateId()));

        ItemInstance instance = itemInstanceRepository.save(new ItemInstance(template, request.quantity()));

        if (request.locationType() == LocationType.ROOM) {
            placeInRoom(instance, Long.parseLong(request.locationId()));
        } else {
            placeInCharacter(instance, UUID.fromString(request.locationId()));
        }

        return instance;
    }

    private void placeInRoom(ItemInstance instance, Long roomId) {
        Room room = roomRepository.findById(roomId)
            .orElseThrow(() -> new NoSuchElementException("Room not found: " + roomId));
        room.addFloorItem(instance);
        roomRepository.save(room);
        gameWorldService.getRoomOptional(roomId).ifPresent(r -> {
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
