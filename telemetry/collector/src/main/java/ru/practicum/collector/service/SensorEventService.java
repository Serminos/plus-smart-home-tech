package ru.practicum.collector.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.practicum.collector.mapper.SensorEventMapper;
import ru.practicum.collector.model.sensor.*;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;

@Slf4j
@Service
@RequiredArgsConstructor
public class SensorEventService {
    private final KafkaTemplate<String, SensorEventAvro> kafkaTemplate;
    private final SensorEventMapper sensorEventMapper;
    @Value("${kafka.topic.sensors:telemetry.sensors.v1}")
    private String sensorTopic;

    public void send(LightSensorEvent event) {
        SensorEventAvro avro = sensorEventMapper.toAvro(event);
        kafkaTemplate.send(sensorTopic, null, event.getTimestamp().toEpochMilli(), avro.getId(), avro);
        log.info("LIGHT_SENSOR_EVENT send: {}", avro);
    }

    public void send(MotionSensorEvent event) {
        SensorEventAvro avro = sensorEventMapper.toAvro(event);
        kafkaTemplate.send(sensorTopic, null, event.getTimestamp().toEpochMilli(), avro.getId(), avro);
        log.info("MOTION_SENSOR_EVENT send: {}", avro);
    }

    public void send(ClimateSensorEvent event) {
        SensorEventAvro avro = sensorEventMapper.toAvro(event);
        kafkaTemplate.send(sensorTopic, null, event.getTimestamp().toEpochMilli(), avro.getId(), avro);
        log.info("CLIMATE_SENSOR_EVENT send: {}", avro);
    }

    public void send(SwitchSensorEvent event) {
        SensorEventAvro avro = sensorEventMapper.toAvro(event);
        kafkaTemplate.send(sensorTopic, null, event.getTimestamp().toEpochMilli(), avro.getId(), avro);
        log.info("SWITCH_SENSOR_EVENT send: {}", avro);
    }

    public void send(TemperatureSensorEvent event) {
        SensorEventAvro avro = sensorEventMapper.toAvro(event);
        kafkaTemplate.send(sensorTopic, null, event.getTimestamp().toEpochMilli(), avro.getId(), avro);
        log.info("TEMPERATURE_SENSOR_EVENT send: {}", avro);
    }
}
