package com.example.theweatherapp.controller;

import com.example.theweatherapp.dto.DailyForecastDto;
import com.example.theweatherapp.dto.WeeklyForecastSummaryDto;
import com.example.theweatherapp.service.WeatherForecastService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/forecast")
@RequiredArgsConstructor
public class WeatherForecastController {

    private final WeatherForecastService weatherForecastService;

    @GetMapping("/daily")
    public ResponseEntity<List<DailyForecastDto>> getDailyForecast(
            @RequestParam @Valid @DecimalMin("-90.0") @DecimalMax("90.0") Double latitude,
            @RequestParam @Valid @DecimalMin("-180.0") @DecimalMax("180.0") Double longitude) {
        return ResponseEntity.ok(weatherForecastService.getDailyForecast(latitude, longitude));
    }

    @GetMapping("/summary")
    public ResponseEntity<WeeklyForecastSummaryDto> getWeeklySummary(
            @RequestParam @Valid @DecimalMin("-90.0") @DecimalMax("90.0") Double latitude,
            @RequestParam @Valid @DecimalMin("-180.0") @DecimalMax("180.0") Double longitude) {
        return ResponseEntity.ok(weatherForecastService.getWeeklySummary(latitude, longitude));
    }
}
