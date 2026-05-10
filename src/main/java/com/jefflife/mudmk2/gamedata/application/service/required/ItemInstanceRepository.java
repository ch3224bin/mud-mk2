package com.jefflife.mudmk2.gamedata.application.service.required;

import com.jefflife.mudmk2.gamedata.application.domain.model.item.ItemInstance;
import org.springframework.data.repository.CrudRepository;

public interface ItemInstanceRepository extends CrudRepository<ItemInstance, Long> {
}
