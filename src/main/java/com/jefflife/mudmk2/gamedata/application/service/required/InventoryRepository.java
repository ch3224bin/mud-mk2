package com.jefflife.mudmk2.gamedata.application.service.required;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.Inventory;
import org.springframework.data.repository.CrudRepository;
import java.util.UUID;

public interface InventoryRepository extends CrudRepository<Inventory, UUID> {
}
