package com.jefflife.mudmk2.gamedata.application.service.provided;

import com.jefflife.mudmk2.gamedata.application.domain.model.martialart.ExternalArtTemplate;
import com.jefflife.mudmk2.gamedata.application.service.model.request.ExternalArtTemplateRequest;

public interface ExternalArtTemplateCreator {
    ExternalArtTemplate create(ExternalArtTemplateRequest request);
}
