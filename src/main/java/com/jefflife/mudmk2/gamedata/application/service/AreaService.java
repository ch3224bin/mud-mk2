package com.jefflife.mudmk2.gamedata.application.service;

import com.jefflife.mudmk2.gamedata.application.domain.model.map.Area;
import com.jefflife.mudmk2.gamedata.application.service.model.request.CreateAreaRequest;
import com.jefflife.mudmk2.gamedata.application.service.model.request.UpdateAreaRequest;
import com.jefflife.mudmk2.gamedata.application.service.provided.CreateAreaUseCase;
import com.jefflife.mudmk2.gamedata.application.service.provided.DeleteAreaUseCase;
import com.jefflife.mudmk2.gamedata.application.service.provided.GetAreaUseCase;
import com.jefflife.mudmk2.gamedata.application.service.provided.UpdateAreaUseCase;
import com.jefflife.mudmk2.gamedata.application.service.required.AreaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.StreamSupport;

@Service
public class AreaService implements CreateAreaUseCase, UpdateAreaUseCase, GetAreaUseCase, DeleteAreaUseCase {
    private final AreaRepository areaRepository;

    public AreaService(AreaRepository areaRepository) {
        this.areaRepository = areaRepository;
    }

    @Override
    public Area createArea(final CreateAreaRequest createAreaRequest) {
        return areaRepository.save(createAreaRequest.toDomain());
    }

    @Override
    public Area getArea(Long id) {
        return areaRepository.findById(id)
                .orElseThrow(IllegalArgumentException::new);
    }

    @Override
    public List<Area> getAreas() {
        return StreamSupport.stream(areaRepository.findAll().spliterator(), false)
                .toList();
    }

    @Override
    public Area updateArea(Long id, UpdateAreaRequest updateAreaRequest) {
        Area area = areaRepository.findById(id)
                .orElseThrow(IllegalArgumentException::new);
        area.changeName(updateAreaRequest.getName());
        return areaRepository.save(area);
    }

    @Override
    public void deleteArea(Long id) {
        Area area = areaRepository.findById(id)
                .orElseThrow(IllegalArgumentException::new);
        areaRepository.delete(area);
    }
}
