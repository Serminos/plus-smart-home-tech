package ru.practicum.collector.kafka;

import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.common.serialization.Serializer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class KafkaSerializer implements Serializer<SpecificRecordBase> {
    private final Map<String, DatumWriter<SpecificRecordBase>> writers = new ConcurrentHashMap<>();

    @Override
    public byte[] serialize(String topic, SpecificRecordBase data) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            if (data == null) return null;

            String schemaName = data.getSchema().getFullName();
            BinaryEncoder encoder = EncoderFactory.get().binaryEncoder(out, null);
            DatumWriter<SpecificRecordBase> writer = writers.computeIfAbsent(
                    schemaName,
                    k -> new SpecificDatumWriter<>(data.getSchema()));
            writer.write(data, encoder);
            encoder.flush();
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Ошибка сериализации Avro", e);
        }
    }
}
