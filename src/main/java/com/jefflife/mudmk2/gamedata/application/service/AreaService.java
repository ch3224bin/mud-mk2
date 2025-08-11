package com.jefflife.mudmk2.gamedata.application.service;

import com.jefflife.mudmk2.gamedata.application.domain.model.map.Area;
import com.jefflife.mudmk2.gamedata.application.service.required.AreaRepository;
import com.jefflife.mudmk2.gamedata.application.port.in.CreateAreaUseCase;
import com.jefflife.mudmk2.gamedata.application.port.in.DeleteAreaUseCase;
import com.jefflife.mudmk2.gamedata.application.port.in.GetAreaUseCase;
import com.jefflife.mudmk2.gamedata.application.port.in.UpdateAreaUseCase;
import com.jefflife.mudmk2.gamedata.application.service.model.request.CreateAreaRequest;
import com.jefflife.mudmk2.gamedata.application.service.model.request.UpdateAreaRequest;
import com.jefflife.mudmk2.gamedata.application.service.model.response.AreaResponse;
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
    public AreaResponse createArea(final CreateAreaRequest createAreaRequest) {
        Area savedArea = areaRepository.save(createAreaRequest.toDomain());
        return AreaResponse.of(savedArea);
    }

    @Override
    public AreaResponse getArea(Long id) {
        Area area = areaRepository.findById(id)
                .orElseThrow(IllegalArgumentException::new);
        return AreaResponse.of(area);
    }

    @Override
    public List<AreaResponse> getAreas() {
        return StreamSupport.stream(areaRepository.findAll().spliterator(), false)
                .map(AreaResponse::of)
                .toList();
    }

    @Override
    public AreaResponse updateArea(Long id, UpdateAreaRequest updateAreaRequest) {
        Area area = areaRepository.findById(id)
                .orElseThrow(IllegalArgumentException::new);
        area.changeName(updateAreaRequest.getName());
        Area savedArea = areaRepository.save(area);
        return AreaResponse.of(savedArea);
    }

    @Override
    public void deleteArea(Long id) {
        Area area = areaRepository.findById(id)
                .orElseThrow(IllegalArgumentException::new);
        areaRepository.delete(area);
    }
}
