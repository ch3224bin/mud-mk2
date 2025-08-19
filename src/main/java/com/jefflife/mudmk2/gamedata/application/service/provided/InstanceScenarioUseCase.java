package com.jefflife.mudmk2.gamedata.application.service.provided;

import com.jefflife.mudmk2.gamedata.application.service.model.request.CreateInstanceScenarioRequest;
import com.jefflife.mudmk2.gamedata.application.service.model.request.UpdateInstanceScenarioRequest;
import com.jefflife.mudmk2.gamedata.application.domain.model.instance.InstanceScenario;

import java.util.List;

public interface InstanceScenarioUseCase {
    interface Create {
        InstanceScenario createInstanceScenario(CreateInstanceScenarioRequest request);
    }

    interface Update {
        InstanceScenario updateInstanceScenario(long id, UpdateInstanceScenarioRequest request);
    }

    interface Get {
        InstanceScenario getInstanceScenario(long id);
        List<InstanceScenario> getAllInstanceScenarios();
        InstanceScenario getInstanceScenarioByTitle(String title);
    }

    interface Delete {
        void deleteInstanceScenario(long id);
    }
}
