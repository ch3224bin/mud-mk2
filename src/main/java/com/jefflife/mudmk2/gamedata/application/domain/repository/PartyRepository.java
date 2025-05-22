package com.jefflife.mudmk2.gamedata.application.domain.repository;

import com.jefflife.mudmk2.gamedata.application.domain.model.party.Party;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PartyRepository extends JpaRepository<Party, Long> {
}
