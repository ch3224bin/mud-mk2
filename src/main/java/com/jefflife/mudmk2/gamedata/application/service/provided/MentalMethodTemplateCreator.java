package com.jefflife.mudmk2.gamedata.application.service.provided;

import com.jefflife.mudmk2.gamedata.application.domain.model.martialart.MentalMethodTemplate;
import com.jefflife.mudmk2.gamedata.application.service.model.request.MentalMethodTemplateRequest;

public interface MentalMethodTemplateCreator {
    MentalMethodTemplate create(MentalMethodTemplateRequest request);
}
