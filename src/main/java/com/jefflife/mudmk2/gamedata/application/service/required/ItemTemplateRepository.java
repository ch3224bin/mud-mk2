package com.jefflife.mudmk2.gamedata.application.service.required;

import com.jefflife.mudmk2.gamedata.application.domain.model.item.ItemTemplate;
import org.springframework.data.repository.CrudRepository;
import java.util.List;

public interface ItemTemplateRepository extends CrudRepository<ItemTemplate, Long> {
    List<ItemTemplate> findByName(String name);
}
