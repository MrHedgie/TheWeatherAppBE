package com.example.theweatherapp.dto;

public record WeeklyForecastSummaryDto(
        double averagePressure,
        double averageSunshineSeconds,
        double weeklyTempMin,
        double weeklyTempMax,
        String summary
) {
}
