package com.example.theweatherapp.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record WeatherApiResponse(
        @JsonProperty("daily") DailyData dailyData
) {
}
