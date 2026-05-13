package com.jefflife.mudmk2.gamedata.application.service;

import com.jefflife.mudmk2.gamedata.application.domain.model.item.*;
import com.jefflife.mudmk2.gamedata.application.service.model.request.ItemTemplateRequest;
import com.jefflife.mudmk2.gamedata.application.service.provided.*;
import com.jefflife.mudmk2.gamedata.application.service.required.ItemTemplateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional
public class ItemTemplateService implements
    ItemTemplateCreator, ItemTemplateFinder, ItemTemplateModifier, ItemTemplateRemover {

    private final ItemTemplateRepository itemTemplateRepository;

    public ItemTemplateService(ItemTemplateRepository itemTemplateRepository) {
        this.itemTemplateRepository = itemTemplateRepository;
    }

    @Override
    public ItemTemplate create(ItemTemplateRequest request) {
        return itemTemplateRepository.save(buildTemplate(request));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemTemplate> findAll() {
        List<ItemTemplate> result = new ArrayList<>();
        itemTemplateRepository.findAll().forEach(result::add);
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemTemplate> findByType(ItemType type) {
        return itemTemplateRepository.findByItemType(type);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemTemplate> findByNameContaining(String name) {
        return itemTemplateRepository.findByNameContaining(name);
    }

    @Override
    @Transactional(readOnly = true)
    public ItemTemplate findById(Long id) {
        return itemTemplateRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("ItemTemplate not found: " + id));
    }

    @Override
    public ItemTemplate update(Long id, ItemTemplateRequest request) {
        ItemTemplate template = itemTemplateRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("ItemTemplate not found: " + id));
        if (template instanceof FoodTemplate f) f.update(request);
        else if (template instanceof WeaponTemplate w) w.update(request);
        else if (template instanceof EquipmentTemplate e) e.update(request);
        else if (template instanceof AccessoryTemplate a) a.update(request);
        else if (template instanceof MartialArtsBookTemplate m) m.update(request);
        else if (template instanceof MissionItemTemplate mi) mi.update(request);
        return template;
    }

    @Override
    public void delete(Long id) {
        if (!itemTemplateRepository.existsById(id)) {
            throw new NoSuchElementException("ItemTemplate not found: " + id);
        }
        itemTemplateRepository.deleteById(id);
    }

    private ItemTemplate buildTemplate(ItemTemplateRequest request) {
        List<StatModifier> statModifiers = request.statModifiers() == null ? List.of() :
            request.statModifiers().stream().map(sm -> sm.toDomain()).toList();

        return switch (request.itemType()) {
            case FOOD -> FoodTemplate.builder()
                .name(request.name()).description(request.description())
                .weight(request.weight()).stackable(request.stackable())
                .hpRecovery(request.hpRecovery() != null ? request.hpRecovery() : 0)
                .mpRecovery(request.mpRecovery() != null ? request.mpRecovery() : 0)
                .apRecovery(request.apRecovery() != null ? request.apRecovery() : 0)
                .build();
            case WEAPON -> WeaponTemplate.builder()
                .name(request.name()).description(request.description())
                .weight(request.weight()).stackable(request.stackable())
                .weaponType(request.weaponType())
                .statModifiers(statModifiers)
                .build();
            case EQUIPMENT -> EquipmentTemplate.builder()
                .name(request.name()).description(request.description())
                .weight(request.weight()).stackable(request.stackable())
                .equipmentSlot(request.equipmentSlot())
                .statModifiers(statModifiers)
                .build();
            case ACCESSORY -> AccessoryTemplate.builder()
                .name(request.name()).description(request.description())
                .weight(request.weight()).stackable(request.stackable())
                .accessoryType(request.accessoryType())
                .statModifiers(statModifiers)
                .build();
            case MARTIAL_ARTS_BOOK -> MartialArtsBookTemplate.builder()
                .name(request.name()).description(request.description())
                .weight(request.weight()).stackable(request.stackable())
                .skillRef(request.skillRef())
                .build();
            case MISSION -> MissionItemTemplate.builder()
                .name(request.name()).description(request.description())
                .weight(request.weight()).stackable(request.stackable())
                .missionItemType(request.missionItemType())
                .targetRef(request.targetRef())
                .build();
        };
    }
}
