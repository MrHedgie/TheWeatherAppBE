package com.example.theweatherapp.service;

import com.example.theweatherapp.dto.DailyForecastDto;
import com.example.theweatherapp.dto.WeeklyForecastSummaryDto;
import com.example.theweatherapp.exception.WeatherApiException;
import com.example.theweatherapp.exception.WeatherDataException;
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

    private void validateWeatherResponse(WeatherApiResponse response) {
        if (response.dailyData() == null) {
            throw new WeatherDataException("Weather response doesn't contain daily data");
        }
        DailyData dailyData = response.dailyData();
        if (dailyData.getDates() == null || dailyData.getDates().isEmpty()) {
            throw new WeatherDataException("Weather response doesn't contain date information");
        }
        int expectedSize = Math.min(dailyData.getDates().size(), FORECAST_DAYS);

        if (dailyData.getWeatherCodes() == null || dailyData.getWeatherCodes().size() < expectedSize) {
            throw new WeatherDataException("Weather response is missing weather codes");
        }
        if (dailyData.getTempMin() == null || dailyData.getTempMin().size() < expectedSize) {
            throw new WeatherDataException("Weather response is missing minimum temperatures");
        }
        if (dailyData.getTempMax() == null || dailyData.getTempMax().size() < expectedSize) {
            throw new WeatherDataException("Weather response is missing maximum temperatures");
        }
        if (dailyData.getSunshineDuration() == null || dailyData.getSunshineDuration().size() < expectedSize) {
            throw new WeatherDataException("Weather response is missing sunshine duration data");
        }
        if (dailyData.getPressure() == null || dailyData.getPressure().size() < expectedSize) {
            throw new WeatherDataException("Weather response is missing pressure data");
        }
    }

    private WeatherApiResponse fetchWeather(double lat, double lon) {
        String url = String.format(
                "%s?latitude=%.4f&longitude=%.4f&daily=weather_code,temperature_2m_max,temperature_2m_min,surface_pressure_mean,sunshine_duration&timezone=auto",
                apiUrl, lat, lon
        );
        try {
            WeatherApiResponse response = restTemplate.getForObject(url, WeatherApiResponse.class);
            if (response == null) {
                throw new WeatherApiException("Weather API Error");
            }
            validateWeatherResponse(response);
            return response;
        } catch (RestClientException e) {
            throw new WeatherApiException("Error fetching weather data");
        }
    }

    private double calculateGeneratedEnergy(double sunshineDuration) {
        if(sunshineDuration < 0){
            return 0;
        }
        return BigDecimal.valueOf(PANEL_POWER * (sunshineDuration / 3600) * PANEL_EFFICIENCY)
                .setScale(DECIMAL_PLACES_SCALE, RoundingMode.HALF_UP)
                .doubleValue();
    }


    public List<DailyForecastDto> getDailyForecast(double lat, double lon) {
        WeatherApiResponse response = fetchWeather(lat, lon);
        DailyData dailyData = response.dailyData();
        List<DailyForecastDto> forecasts = new ArrayList<>();
        for (int i = 0; i < FORECAST_DAYS; i++) {
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
                BigDecimal.valueOf(dailyData.getPressure().stream().mapToDouble(Double::doubleValue).average().orElse(0)).setScale(1, RoundingMode.HALF_UP).doubleValue(),
                BigDecimal.valueOf(dailyData.getSunshineDuration().stream().mapToDouble(Double::doubleValue).average().orElse(0)).setScale(1, RoundingMode.HALF_UP).doubleValue(),
                dailyData.getTempMin().stream().mapToDouble(Double::doubleValue).min().orElse(Double.NaN),
                dailyData.getTempMax().stream().mapToDouble(Double::doubleValue).max().orElse(Double.NaN),
                dailyData.getWeatherCodes().stream().filter(code -> code >= 60 && code <= 99).count() >= 4 ? "Rainy" : "Not Rainy"
        );
    }
}
