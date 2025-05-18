package com.jefflife.mudmk2.gamedata.application.domain.repository;

import com.jefflife.mudmk2.gamedata.application.domain.model.instance.InstanceScenario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InstanceScenarioRepository extends JpaRepository<InstanceScenario, Long> {
    List<InstanceScenario> findByTitleContaining(String title);
    Optional<InstanceScenario> findByTitle(String title);
}
