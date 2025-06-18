package com.example.theweatherapp;

import com.example.theweatherapp.controller.WeatherForecastController;
import com.example.theweatherapp.dto.DailyForecastDto;
import com.example.theweatherapp.dto.WeeklyForecastSummaryDto;
import com.example.theweatherapp.service.WeatherForecastService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WeatherForecastController.class)
class WeatherForecastControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WeatherForecastService weatherForecastService;

    @Test
    void shouldReturnDailyForecastWithValidCoordinates() throws Exception {
        // Given
        double latitude = 52.2297;
        double longitude = 21.0122;
        List<DailyForecastDto> expectedForecast = List.of(
                new DailyForecastDto("2024-01-15", 1, 5.2, 12.8, 45.5),
                new DailyForecastDto("2024-01-16", 2, 3.1, 10.4, 38.2),
                new DailyForecastDto("2024-01-17", 0, 7.8, 15.6, 52.1)
        );

        when(weatherForecastService.getDailyForecast(latitude, longitude))
                .thenReturn(expectedForecast);

        // When & Then
        mockMvc.perform(get("/api/forecast/daily")
                        .param("latitude", String.valueOf(latitude))
                        .param("longitude", String.valueOf(longitude))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].date").value("2024-01-15"))
                .andExpect(jsonPath("$[0].weatherCode").value(1))
                .andExpect(jsonPath("$[0].tempMin").value(5.2))
                .andExpect(jsonPath("$[0].tempMax").value(12.8))
                .andExpect(jsonPath("$[0].estimatedGeneratedEnergy").value(45.5))
                .andExpect(jsonPath("$[1].date").value("2024-01-16"))
                .andExpect(jsonPath("$[2].date").value("2024-01-17"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"90.1", "91", "180", "-90.1", "-91"})
    void shouldReturnBadRequestWithInvalidLatitude(String invalidLatitude) throws Exception {
        mockMvc.perform(get("/api/forecast/daily")
                        .param("latitude", invalidLatitude)
                        .param("longitude", "0.0")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @ValueSource(strings = {"180.1", "181", "360", "-180.1", "-181"})
    void shouldReturnBadRequestWithInvalidLongitude(String invalidLongitude) throws Exception {
        mockMvc.perform(get("/api/forecast/daily")
                        .param("latitude", "0.0")
                        .param("longitude", invalidLongitude)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenLatitudeMissing() throws Exception {
        mockMvc.perform(get("/api/forecast/daily")
                        .param("longitude", "21.0122")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenLongitudeMissing() throws Exception {
        mockMvc.perform(get("/api/forecast/daily")
                        .param("latitude", "52.2297")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWithNonNumericCoordinates() throws Exception {
        mockMvc.perform(get("/api/forecast/daily")
                        .param("latitude", "invalid")
                        .param("longitude", "21.0122")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnWeeklySummaryWithValidCoordinates() throws Exception {
        // Given
        double latitude = 52.2297;
        double longitude = 21.0122;
        WeeklyForecastSummaryDto expectedSummary = new WeeklyForecastSummaryDto(
                1013.25,
                25200.0,
                -2.5,
                18.3,
                "Rainy"
        );

        when(weatherForecastService.getWeeklySummary(latitude, longitude))
                .thenReturn(expectedSummary);

        // When & Then
        mockMvc.perform(get("/api/forecast/summary")
                        .param("latitude", String.valueOf(latitude))
                        .param("longitude", String.valueOf(longitude))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.averagePressure").value(1013.25))
                .andExpect(jsonPath("$.averageSunshineSeconds").value(25200.0))
                .andExpect(jsonPath("$.weeklyTempMin").value(-2.5))
                .andExpect(jsonPath("$.weeklyTempMax").value(18.3))
                .andExpect(jsonPath("$.summary").value("Z opadami"));
    }
}
