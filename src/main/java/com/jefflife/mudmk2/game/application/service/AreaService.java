package com.jefflife.mudmk2.game.application.service;

import com.jefflife.mudmk2.game.application.domain.model.map.Area;
import com.jefflife.mudmk2.game.application.domain.repository.AreaRepository;
import com.jefflife.mudmk2.game.application.port.in.CreateAreaUseCase;
import com.jefflife.mudmk2.game.application.port.in.GetAreaUseCase;
import com.jefflife.mudmk2.game.application.port.in.UpdateAreaUseCase;
import com.jefflife.mudmk2.game.application.service.model.request.CreateAreaRequest;
import com.jefflife.mudmk2.game.application.service.model.request.UpdateAreaRequest;
import com.jefflife.mudmk2.game.application.service.model.response.AreaResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.StreamSupport;

@Service
public class AreaService implements CreateAreaUseCase, UpdateAreaUseCase, GetAreaUseCase {
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
}
