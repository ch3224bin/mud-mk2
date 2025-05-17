package com.jefflife.mudmk2.gameplay.adapter.out.eventpublisher.chat;

import com.jefflife.mudmk2.gameplay.application.port.out.SendWeatherMessagePort;
import com.jefflife.mudmk2.gameplay.application.service.model.template.WeatherVariables;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Component
public class WeatherMessageSender implements SendWeatherMessagePort {
    private final TemplateEngine templateEngine;
    private final ChatEventPublisher chatEventPublisher;

    public WeatherMessageSender(final TemplateEngine templateEngine, final ChatEventPublisher chatEventPublisher) {
        this.templateEngine = templateEngine;
        this.chatEventPublisher = chatEventPublisher;
    }

    @Override
    public void sendMessage(final WeatherVariables weatherVariables) {
        final Context context = new Context();
        context.setVariable("weatherType", weatherVariables.weatherType());
        chatEventPublisher.sendSystemMessage(templateEngine.process("gameplay/weather", context));
    }
}
