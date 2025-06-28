package ru.yandex.practicum.serializer;

import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AggregatorAvroSerializer implements Serializer<SpecificRecordBase> {
    private final Map<String, DatumWriter<SpecificRecordBase>> writersCache = new ConcurrentHashMap<>();
    private final EncoderFactory encoderFactory = EncoderFactory.get();

    @Override
    public byte[] serialize(String topic, SpecificRecordBase data) {
        if (data == null) return null;

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            BinaryEncoder encoder = encoderFactory.directBinaryEncoder(out, null);
            DatumWriter<SpecificRecordBase> writer = writersCache.computeIfAbsent(
                    data.getSchema().getFullName(),
                    k -> new SpecificDatumWriter<>(data.getSchema())
            );

            writer.write(data, encoder);
            encoder.flush();
            return out.toByteArray();
        } catch (IOException e) {
            throw new SerializationException("Serialization error for topic: " + topic, e);
        }
    }
}