package ru.yandex.practicum.serializer;

import org.apache.avro.Schema;
import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BaseAvroDeserializer<T extends SpecificRecordBase> implements Deserializer<T> {
    private final DecoderFactory decoderFactory = DecoderFactory.get();
    private final Map<String, DatumReader<T>> readersCache = new ConcurrentHashMap<>();
    private final Schema schema;

    public BaseAvroDeserializer(Schema schema) {
        this.schema = schema;
    }

    @Override
    public T deserialize(String topic, byte[] data) {
        if (data == null) return null;

        try {
            BinaryDecoder decoder = decoderFactory.binaryDecoder(data, null);
            DatumReader<T> reader = readersCache.computeIfAbsent(
                    schema.getFullName(),
                    k -> new SpecificDatumReader<>(schema)
            );

            return reader.read(null, decoder);
        } catch (IOException e) {
            throw new SerializationException("Deserialization error for topic: " + topic, e);
        }
    }
}
