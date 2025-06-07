package ru.yandex.practicum.processor;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.handler.hub.HubEventHandler;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
public class HubEventProcessor implements Runnable {
    @Value("${kafka.consumer.poll-timeout:1000}")
    private long pollTimeoutMillis;
    @Value("${kafka.consumer.close-timeout:5000}")
    private long closeTimeoutMillis;
    @Value("${kafka.topic.hubs:telemetry.hubs.v1}")
    private String hubTopic;
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private final Consumer<String, HubEventAvro> hubConsumer;
    private final Map<String, HubEventHandler> hubHandlers;

    public HubEventProcessor(
            Consumer<String, HubEventAvro> hubConsumer,
            Set<HubEventHandler> hubHandlers
    ) {
        this.hubConsumer = hubConsumer;
        this.hubHandlers = hubHandlers.stream()
                .collect(Collectors.toMap(
                        HubEventHandler::getType,
                        Function.identity(),
                        (existing, replacement) -> existing
                ));
    }

    @Override
    public void run() {
        if (isRunning.getAndSet(true)) {
            log.warn("Processor is already running");
            return;
        }

        try {
            hubConsumer.subscribe(Collections.singletonList(hubTopic));
            Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));

            processMessages();
        } catch (WakeupException e) {
            log.info("Wakeup signal received");
        } catch (Exception e) {
            log.error("Fatal error in processor", e);
        } finally {
            closeConsumer();
        }
    }

    private void processMessages() {
        while (isRunning.get()) {
            try {
                ConsumerRecords<String, HubEventAvro> records =
                        hubConsumer.poll(Duration.ofMillis(pollTimeoutMillis));

                if (records.isEmpty()) continue;

                processRecords(records);
                commitSync();
            } catch (WakeupException e) {
                log.info("Wakeup during polling");
                break;
            } catch (Exception e) {
                log.error("Error processing batch", e);
                sleepOnError();
            }
        }
    }
    @Retryable(
            value = {DataAccessException.class, KafkaException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    private void processRecords(ConsumerRecords<String, HubEventAvro> records) {
        for (ConsumerRecord<String, HubEventAvro> record : records) {
            try {
                processSingleRecord(record);
            } catch (Exception e) {
                log.error("Error processing record [{}:{}]",
                        record.topic(), record.offset(), e);
            }
        }
    }

    private void processSingleRecord(ConsumerRecord<String, HubEventAvro> record) {
        HubEventAvro hubEvent = record.value();
        String payloadType = hubEvent.getPayload().getClass().getName();

        log.debug("Processing hub event [{}]", record.key());
        HubEventHandler handler = hubHandlers.get(payloadType);

        if (handler == null) {
            log.warn("No handler found for payload type: {}", payloadType);
            return;
        }

        handler.handle(hubEvent);
    }

    private void commitSync() {
        try {
            hubConsumer.commitSync();
        } catch (Exception e) {
            log.error("Commit failed", e);
        }
    }

    private void sleepOnError() {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

    public void shutdown() {
        if (isRunning.compareAndSet(true, false)) {
            log.info("Shutting down processor");
            hubConsumer.wakeup();
        }
    }

    private void closeConsumer() {
        try {
            hubConsumer.close(Duration.ofSeconds(closeTimeoutMillis));
            log.info("Consumer closed successfully");
        } catch (Exception e) {
            log.error("Error closing consumer", e);
        }
    }
}