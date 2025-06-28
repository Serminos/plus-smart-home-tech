package ru.yandex.practicum.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

@Configuration
public class RetryConfig {

    @Bean
    public RetryTemplate hubRetryTemplate(
            @Value("${retry.max-attempts:3}") int maxAttempts,
            @Value("${retry.initial-interval:1000}") long initialInterval,
            @Value("${retry.multiplier:2}") double multiplier
    ) {
        return buildRetryTemplate(maxAttempts, initialInterval, multiplier);
    }

    @Bean
    public RetryTemplate snapshotRetryTemplate(
            @Value("${retry.max-attempts:3}") int maxAttempts,
            @Value("${retry.initial-interval:1000}") long initialInterval,
            @Value("${retry.multiplier:2}") double multiplier
    ) {
        return buildRetryTemplate(maxAttempts, initialInterval, multiplier);
    }

    private RetryTemplate buildRetryTemplate(int maxAttempts, long initialInterval, double multiplier) {
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(initialInterval);
        backOffPolicy.setMultiplier(multiplier);

        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(maxAttempts);

        RetryTemplate retryTemplate = new RetryTemplate();
        retryTemplate.setBackOffPolicy(backOffPolicy);
        retryTemplate.setRetryPolicy(retryPolicy);

        return retryTemplate;
    }
}
