package ru.yandex.practicum.processor;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProcessorRunner implements ApplicationRunner {
    private final HubEventProcessor hubEventProcessor;
    private final SnapshotProcessor snapshotProcessor;

    @Override
    public void run(ApplicationArguments args) {
        new Thread(hubEventProcessor, "HubEventProcessor").start();
        new Thread(snapshotProcessor, "SnapshotProcessor").start();
    }
}
