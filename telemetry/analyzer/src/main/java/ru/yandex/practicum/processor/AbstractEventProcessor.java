package ru.yandex.practicum.processor;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.support.RetryTemplate;

import java.time.Duration;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public abstract class AbstractEventProcessor<T> implements Runnable {
    @Value("${kafka.consumer.poll-timeout:1000}")
    protected long pollTimeoutMillis;
    @Value("${kafka.consumer.close-timeout:5000}")
    protected long closeTimeoutMillis;
    protected final AtomicBoolean isRunning = new AtomicBoolean(false);
    protected final Consumer<String, T> consumer;
    protected final String topicName;
    protected final RetryTemplate retryTemplate;

    protected AbstractEventProcessor(
            Consumer<String, T> consumer,
            String topicName,
            RetryTemplate retryTemplate
    ) {
        this.consumer = consumer;
        this.topicName = topicName;
        this.retryTemplate = retryTemplate;
    }

    @Override
    public void run() {
        if (!isRunning.compareAndSet(false, true)) {
            log.warn("Processor is already running");
            return;
        }

        try {
            Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
            consumer.subscribe(Collections.singletonList(topicName));
            log.info("Started consuming from topic: {}", topicName);
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
                ConsumerRecords<String, T> records = consumer.poll(Duration.ofMillis(pollTimeoutMillis));
                if (records.isEmpty()) continue;

                processRecords(records);
                commitSync();
            } catch (WakeupException e) {
                log.info("Wakeup during polling");
                break;
            } catch (Exception e) {
                log.error("Error processing batch: {}", e.getMessage());
                sleepOnError();
            }
        }
    }

    protected abstract void processRecords(ConsumerRecords<String, T> records);

    protected void commitSync() {
        try {
            consumer.commitSync();
        } catch (Exception e) {
            log.error("Commit failed: {}", e.getMessage());
        }
    }

    protected void sleepOnError() {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

    public void shutdown() {
        if (isRunning.compareAndSet(true, false)) {
            log.info("Shutting down processor");
            consumer.wakeup();
        }
    }

    private void closeConsumer() {
        try {
            consumer.close(Duration.ofMillis(closeTimeoutMillis));
            log.info("Consumer closed successfully");
        } catch (Exception e) {
            log.error("Error closing consumer: {}", e.getMessage());
        }
    }
}