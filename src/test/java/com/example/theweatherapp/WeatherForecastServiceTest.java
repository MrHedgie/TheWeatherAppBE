package com.example.theweatherapp;

import com.example.theweatherapp.exception.WeatherApiException;
import com.example.theweatherapp.model.DailyData;
import com.example.theweatherapp.model.WeatherApiResponse;
import com.example.theweatherapp.service.WeatherForecastService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WeatherForecastServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private WeatherForecastService weatherForecastService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(weatherForecastService, "apiUrl", "https://api.open-meteo.com/v1/forecast");
    }

    private WeatherApiResponse mockValidResponse() {
        DailyData dailyData = new DailyData();

        List<String> dates = IntStream.range(0, 7)
                .mapToObj(i -> LocalDate.now().plusDays(i).toString())
                .toList();

        dailyData.setDates(dates);
        dailyData.setWeatherCodes(Arrays.asList(0, 1, 2, 61, 63, 80, 95));
        dailyData.setTempMin(Arrays.asList(10.0, 11.0, 9.5, 8.0, 7.0, 12.0, 13.0));
        dailyData.setTempMax(Arrays.asList(20.0, 19.0, 18.0, 21.0, 23.0, 22.0, 24.0));
        dailyData.setSunshineDuration(Arrays.asList(3600.0, 1800.0, 0.0, 5400.0, 3600.0, 7200.0, 3600.0));
        dailyData.setPressure(Arrays.asList(1012.0, 1010.0, 1008.0, 1005.0, 1007.0, 1009.0, 1011.0));

        return new WeatherApiResponse(dailyData);
    }

    @Test
    void shouldReturnValidDailyForecasts() {
        when(restTemplate.getForObject(anyString(), eq(WeatherApiResponse.class)))
                .thenReturn(mockValidResponse());

        var forecasts = weatherForecastService.getDailyForecast(50.0, 20.0);

        assertEquals(7, forecasts.size());
        assertEquals(10.0, forecasts.getFirst().tempMin());
        assertEquals(20.0, forecasts.getFirst().tempMax());
        assertEquals(0.5, forecasts.getFirst().estimatedGeneratedEnergy());
    }

    @Test
    void shouldReturnValidWeeklySummary() {
        when(restTemplate.getForObject(anyString(), eq(WeatherApiResponse.class)))
                .thenReturn(mockValidResponse());

        var summary = weatherForecastService.getWeeklySummary(50.0, 20.0);

        assertEquals(1008.9, summary.averagePressure()); // Rounded
        assertEquals(3600, summary.averageSunshineSeconds()); // Rounded
        assertEquals(7.0, summary.weeklyTempMin());
        assertEquals(24.0, summary.weeklyTempMax());
        assertEquals("Rainy", summary.summary());
    }
    @Test
    void shouldThrowIfWeatherResponseIsNull() {
        when(restTemplate.getForObject(anyString(), eq(WeatherApiResponse.class)))
                .thenReturn(null);

        assertThrows(WeatherApiException.class, () -> weatherForecastService.getDailyForecast(50.0, 20.0));
    }
    @Test
    void shouldHandleNegativeSunshine() {
        var modified = mockValidResponse();
        modified.dailyData().getSunshineDuration().set(0, -100.0);

        when(restTemplate.getForObject(anyString(), eq(WeatherApiResponse.class)))
                .thenReturn(modified);

        var forecasts = weatherForecastService.getDailyForecast(50.0, 20.0);

        assertEquals(0.0, forecasts.getFirst().estimatedGeneratedEnergy());
    }
}
