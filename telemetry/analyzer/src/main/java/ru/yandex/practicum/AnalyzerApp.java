package ru.yandex.practicum;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.retry.annotation.EnableRetry;

@EnableRetry
@SpringBootApplication
@ConfigurationPropertiesScan
public class AnalyzerApp {
    public static void main(String[] args) {
        SpringApplication.run(AnalyzerApp.class, args);
    }
}