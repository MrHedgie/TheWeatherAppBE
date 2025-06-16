package com.example.theweatherapp.exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(WeatherApiException.class)
    public ResponseEntity<String> handleWeatherApiException() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("Unable to fetch weather data");
    }

    @ExceptionHandler(WeatherDataException.class)
    public ResponseEntity<String> handleWeatherDataException() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("Unable to process weather data");
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<String> handleMissingServletRequestParameterException() {
        return ResponseEntity.badRequest().body("Both latitude and longitude are required.");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<String> handleMethodArgumentTypeMismatchException() {
        return ResponseEntity.badRequest().body("Invalid argument type. Please provide numeric values for latitude and longitude.");
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<String> handleConstraintViolationException() {
        return ResponseEntity.badRequest().body("Latitude must be between -90 and 90, longitude must be between -180 and 180.");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException() {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unexpected error occurred.");
    }
}
