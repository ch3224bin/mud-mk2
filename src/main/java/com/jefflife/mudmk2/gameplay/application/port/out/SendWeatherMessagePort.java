package com.jefflife.mudmk2.gameplay.application.port.out;

import com.jefflife.mudmk2.gameplay.application.service.model.template.WeatherVariables;

public interface SendWeatherMessagePort {
    void sendMessage(WeatherVariables weatherVariables);
}
