package com.jefflife.mudmk2.gamedata.application.service.required;

import com.jefflife.mudmk2.gamedata.application.domain.model.party.Party;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface PartyRepository extends JpaRepository<Party, UUID> {
}
