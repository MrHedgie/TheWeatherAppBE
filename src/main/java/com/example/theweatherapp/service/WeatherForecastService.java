package com.example.theweatherapp.service;

import com.example.theweatherapp.dto.DailyForecastDto;
import com.example.theweatherapp.dto.WeeklyForecastSummaryDto;
import com.example.theweatherapp.model.DailyData;
import com.example.theweatherapp.model.WeatherApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WeatherForecastService {

    private static final double PANEL_POWER = 2.5;
    private static final double PANEL_EFFICIENCY = 0.2;
    private static final int FORECAST_DAYS = 7;
    private static final int DECIMAL_PLACES_SCALE = 2;
    private final RestTemplate restTemplate;
    @Value("${open-meteo.api.url}")
    private String apiUrl;

    private WeatherApiResponse fetchWeather(double lat, double lon) {
        String url = String.format(
                "%s?latitude=%.4f&longitude=%.4f&daily=weather_code,temperature_2m_max,temperature_2m_min,surface_pressure_mean,sunshine_duration&timezone=auto",
                apiUrl, lat, lon
        );
        try{
            return restTemplate.getForObject(url, WeatherApiResponse.class);
        } catch (RestClientException e) {
            throw new RuntimeException(e);
        }
    }

    private double calculateGeneratedEnergy(double sunshineDuration){
        return BigDecimal.valueOf(PANEL_POWER * (sunshineDuration / 3600) * PANEL_EFFICIENCY)
                .setScale(DECIMAL_PLACES_SCALE, RoundingMode.HALF_UP)
                .doubleValue();
    }

    public List<DailyForecastDto> getDailyForecast(double lat, double lon) {
        WeatherApiResponse response = fetchWeather(lat, lon);
        DailyData dailyData = response.dailyData();
        List<DailyForecastDto> forecasts = new ArrayList<>();
        for(int i = 0; i < FORECAST_DAYS; i++) {
            forecasts.add(new DailyForecastDto(
                    dailyData.getDates().get(i),
                    dailyData.getWeatherCodes().get(i),
                    dailyData.getTempMin().get(i),
                    dailyData.getTempMax().get(i),
                    calculateGeneratedEnergy(dailyData.getSunshineDuration().get(i))
            ));
        }
        return forecasts;
    }

    public WeeklyForecastSummaryDto getWeeklySummary(double lat, double lon) {
        WeatherApiResponse response = fetchWeather(lat, lon);
        DailyData dailyData = response.dailyData();
        return new WeeklyForecastSummaryDto(
                BigDecimal.valueOf(dailyData.getPressure().stream().mapToDouble(d->d).average().orElse(0)).setScale(1, RoundingMode.HALF_UP).doubleValue(),
                BigDecimal.valueOf(dailyData.getSunshineDuration().stream().mapToDouble(d->d).average().orElse(0)).setScale(1, RoundingMode.HALF_UP).doubleValue(),
                dailyData.getTempMin().stream().mapToDouble(d->d).min().orElse(Integer.MIN_VALUE),
                dailyData.getTempMax().stream().mapToDouble(d->d).max().orElse(Integer.MAX_VALUE),
                dailyData.getWeatherCodes().stream().filter(code -> code >= 60 && code <= 99).count() >= 4 ? "Rainy" : "Not Rainy"
        );
    }
}
