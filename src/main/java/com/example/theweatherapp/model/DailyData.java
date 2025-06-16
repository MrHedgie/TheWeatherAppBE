package com.example.theweatherapp.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class DailyData {

    @JsonProperty("time")
    private List<String> dates; //format: RRRR-MM-DD
    @JsonProperty("weather_code")
    private List<Integer> weatherCodes;
    @JsonProperty("temperature_2m_min")
    private List<Double> tempMin; //in Celsius
    @JsonProperty("temperature_2m_max")
    private List<Double> tempMax; //in Celsius
    @JsonProperty("surface_pressure_mean")
    private List<Double> pressure; //in hPa
    @JsonProperty("sunshine_duration")
    private List<Double> sunshineDuration; //in seconds
}
