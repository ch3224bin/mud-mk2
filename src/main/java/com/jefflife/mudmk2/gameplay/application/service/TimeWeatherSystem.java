package com.jefflife.mudmk2.gameplay.application.service;

import com.jefflife.mudmk2.gameplay.application.service.required.SendGameTimeMessagePort;
import com.jefflife.mudmk2.gameplay.application.service.required.SendWeatherMessagePort;
import com.jefflife.mudmk2.gameplay.application.service.model.template.GameTimeVariables;
import com.jefflife.mudmk2.gameplay.application.service.model.template.WeatherVariables;
import com.jefflife.mudmk2.gameplay.application.tick.TickListener;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Random;

@Slf4j
@Component
public class TimeWeatherSystem implements TickListener {

    private static final int TICKS_PER_GAME_HOUR = 600; // 60틱 = 1시간

    private final SendGameTimeMessagePort sendGameTimeMessagePort;
    private final SendWeatherMessagePort sendWeatherMessagePort;

    private int gameHour = 6; // 게임 시작은 아침 6시
    private String currentWeather = "맑음";
    private final String[] weatherTypes = {"맑음", "흐림", "비", "폭풍", "안개"};
    private final Random random = new Random();

    public TimeWeatherSystem(
            final SendGameTimeMessagePort sendGameTimeMessagePort,
            final SendWeatherMessagePort sendWeatherMessagePort
    ) {
        this.sendGameTimeMessagePort = sendGameTimeMessagePort;
        this.sendWeatherMessagePort = sendWeatherMessagePort;
    }

    @Async("taskExecutor")
    @Override
    public void onTick(long tickCount) {
        // 게임 시간 업데이트
        if (tickCount % TICKS_PER_GAME_HOUR == 0) {
            gameHour = (gameHour + 1) % 24;
            broadcastTimeChange();

            // 4시간마다 날씨 변화 가능성
            if (gameHour % 4 == 0 && random.nextDouble() < 0.3) {
                changeWeather();
            }
        }
    }

    private void broadcastTimeChange() {
        GameTimeVariables timeUpdate = new GameTimeVariables(gameHour, getDayPeriod());
        sendGameTimeMessagePort.sendMessage(timeUpdate);
        log.info("게임 시간 변경: {}시 ({})", gameHour, getDayPeriod());
    }

    private void changeWeather() {
        String oldWeather = currentWeather;
        // 현재와 다른 날씨 선택
        do {
            currentWeather = weatherTypes[random.nextInt(weatherTypes.length)];
        } while (currentWeather.equals(oldWeather));

        WeatherVariables weatherUpdate = new WeatherVariables(currentWeather);
        sendWeatherMessagePort.sendMessage(weatherUpdate);
        log.info("날씨 변경: {} -> {}", oldWeather, currentWeather);
    }

    public String getDayPeriod() {
        if (gameHour >= 5 && gameHour < 12) return "아침";
        if (gameHour >= 12 && gameHour < 18) return "낮";
        if (gameHour >= 18 && gameHour < 22) return "저녁";
        return "밤";
    }

    // 현재 시간에 따른 가시성 변화
    public int getVisibilityModifier() {
        if (gameHour >= 9 && gameHour < 18) return 0; // 낮에는 정상 가시성
        if (gameHour >= 18 && gameHour < 21) return -1; // 저녁에는 약간 감소
        if ((gameHour >= 21 && gameHour < 24) || (gameHour >= 0 && gameHour < 5)) return -3; // 밤에는 크게 감소
        return -2; // 새벽에는 중간 정도 감소
    }

    // 현재 날씨에 따른 가시성/이동속도 변화
    public WeatherEffects getWeatherEffects() {
        return switch (currentWeather) {
            case "맑음" -> new WeatherEffects(0, 0);
            case "흐림" -> new WeatherEffects(-1, 0);
            case "비" -> new WeatherEffects(-2, -1);
            case "폭풍" -> new WeatherEffects(-4, -2);
            case "안개" -> new WeatherEffects(-5, -1);
            default -> new WeatherEffects(0, 0);
        };
    }

    @Data
    @AllArgsConstructor
    public static class WeatherEffects {
        private int visibilityMod;
        private int movementMod;
    }
}
