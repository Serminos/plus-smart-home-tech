package ru.yandex.practicum.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.handler.snapshot.SnapshotHandler;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.time.Duration;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Component
@RequiredArgsConstructor
public class SnapshotProcessor implements Runnable {
    @Value("${kafka.topic.snapshots:telemetry.snapshots.v1}")
    private String snapshotTopic;
    @Value("${kafka.consumer.poll-timeout:1000}")
    private long pollTimeoutMillis;
    @Value("${kafka.consumer.close-timeout:5000}")
    private long closeTimeoutMillis;
    private final Consumer<String, SensorsSnapshotAvro> snapshotConsumer;
    private final SnapshotHandler snapshotHandler;
    private final AtomicBoolean isRunning = new AtomicBoolean(false);

    public void run() {
        if (isRunning.getAndSet(true)) {
            log.warn("Processor is already running");
            return;
        }

        try {
            snapshotConsumer.subscribe(Collections.singletonList(snapshotTopic));
            Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));

            processMessages();
        } catch (WakeupException e) {
            log.info("Wakeup signal received");
        } catch (Exception e) {
            log.error("Fatal error in processor: {}", e.getMessage(), e);
        } finally {
            closeConsumer();
        }
    }

    private void processMessages() {
        while (isRunning.get()) {
            try {
                ConsumerRecords<String, SensorsSnapshotAvro> records =
                        snapshotConsumer.poll(Duration.ofMillis(pollTimeoutMillis));

                if (records.isEmpty()) continue;

                processRecords(records);
                commitSync();
            } catch (WakeupException e) {
                log.info("Wakeup during polling");
                break;
            } catch (Exception e) {
                log.error("Error processing batch: {}", e.getMessage(), e);
                sleepOnError();
            }
        }
    }

    private void processRecords(ConsumerRecords<String, SensorsSnapshotAvro> records) {
        for (ConsumerRecord<String, SensorsSnapshotAvro> record : records) {
            try {
                log.debug("Received Kafka message [{}:{}]", record.topic(), record.offset());
                SensorsSnapshotAvro snapshot = record.value();
                snapshotHandler.handleSnapshot(snapshot);
            } catch (Exception e) {
                log.error("Error processing record [{}:{}]: {}",
                        record.topic(), record.offset(), e.getMessage(), e);
            }
        }
    }

    private void commitSync() {
        try {
            snapshotConsumer.commitSync();
        } catch (Exception e) {
            log.error("Commit failed: {}", e.getMessage(), e);
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
            snapshotConsumer.wakeup();
        }
    }

    private void closeConsumer() {
        try {
            snapshotConsumer.close(Duration.ofSeconds(closeTimeoutMillis));
            log.info("Consumer closed successfully");
        } catch (Exception e) {
            log.error("Error closing consumer: {}", e.getMessage(), e);
        }
    }
}

