package com.example.theweatherapp.dto;

public record DailyForecastDto(
        String date,
        int weatherCode,
        double tempMin,
        double tempMax,
        double estimatedGeneratedEnergy
) {
}
