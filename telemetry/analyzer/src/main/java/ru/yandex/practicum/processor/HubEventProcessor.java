package ru.yandex.practicum.processor;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.handler.hub.HubEventHandler;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
public class HubEventProcessor extends AbstractEventProcessor<HubEventAvro> {
    private final Map<String, HubEventHandler> hubHandlers;

    public HubEventProcessor(
            Consumer<String, HubEventAvro> hubConsumer,
            @Value("${kafka.topic.hubs:telemetry.hubs.v1}") String topicName,
            Set<HubEventHandler> hubHandlers,
            RetryTemplate hubRetryTemplate
    ) {
        super(hubConsumer, topicName, hubRetryTemplate);
        this.hubHandlers = hubHandlers.stream()
                .collect(Collectors.toMap(
                        HubEventHandler::getType,
                        Function.identity(),
                        (existing, replacement) -> existing
                ));
    }

    @Override
    protected void processRecords(ConsumerRecords<String, HubEventAvro> records) {
        for (ConsumerRecord<String, HubEventAvro> record : records) {
            try {
                processSingleRecordWithRetry(record);
            } catch (Exception e) {
                log.error("Permanent error processing record [{}:{}] - {}",
                        record.topic(), record.offset(), e.getMessage());
            }
        }
    }

    private void processSingleRecordWithRetry(ConsumerRecord<String, HubEventAvro> record) {
        retryTemplate.execute(context -> {
            try {
                return processSingleRecord(record);
            } catch (RuntimeException e) {
                log.warn("Retryable error processing record [{}:{}] (attempt {}): {}",
                        record.topic(), record.offset(),
                        context.getRetryCount() + 1, e.getMessage());
                throw e;
            }
        });
    }

    private Void processSingleRecord(ConsumerRecord<String, HubEventAvro> record) {
        HubEventAvro hubEvent = record.value();
        String payloadType = hubEvent.getPayload().getClass().getName();

        log.debug("Processing hub event [{}]", record.key());
        HubEventHandler handler = hubHandlers.get(payloadType);

        if (handler == null) {
            log.warn("No handler found for payload type: {}", payloadType);
            return null;
        }

        handler.handle(hubEvent);
        return null;
    }
}