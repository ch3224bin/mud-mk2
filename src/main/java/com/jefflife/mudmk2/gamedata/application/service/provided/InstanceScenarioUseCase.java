package com.jefflife.mudmk2.gamedata.application.service.provided;

import com.jefflife.mudmk2.gamedata.application.service.model.request.CreateInstanceScenarioRequest;
import com.jefflife.mudmk2.gamedata.application.service.model.request.UpdateInstanceScenarioRequest;
import com.jefflife.mudmk2.gamedata.application.service.model.response.InstanceScenarioResponse;

import java.util.List;

public interface InstanceScenarioUseCase {
    interface Create {
        InstanceScenarioResponse createInstanceScenario(CreateInstanceScenarioRequest request);
    }

    interface Update {
        InstanceScenarioResponse updateInstanceScenario(long id, UpdateInstanceScenarioRequest request);
    }

    interface Get {
        InstanceScenarioResponse getInstanceScenario(long id);
        List<InstanceScenarioResponse> getAllInstanceScenarios();
        InstanceScenarioResponse getInstanceScenarioByTitle(String title);
    }

    interface Delete {
        void deleteInstanceScenario(long id);
    }
}
