package com.example.theweatherapp.dto;

public record WeeklyForecastSummaryDto(
        double averagePressure,
        double averageSunshineHours,
        double weeklyTempMin,
        double weeklyTempMax
) {
}
