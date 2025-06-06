package ru.practicum.collector;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication(scanBasePackages = "ru.practicum.collector")
public class CollectorApp {
    public static void main(String[] args) {
        SpringApplication.run(CollectorApp.class,args);
    }
}