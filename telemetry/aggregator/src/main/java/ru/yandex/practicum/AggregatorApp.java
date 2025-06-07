package ru.yandex.practicum;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
@ConfigurationPropertiesScan
public class AggregatorApp {
    private static final Logger log = LoggerFactory.getLogger(AggregatorApp.class);

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(AggregatorApp.class, args);

        try {
            AggregationStarter aggregator = context.getBean(AggregationStarter.class);
            aggregator.start();
        } catch (Exception e) {
            log.error("Application failed", e);
            context.close();
            System.exit(1);
        }
    }
}