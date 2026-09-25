package com.shawarmashop.tests.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.shawarmashop.tests.support.Json;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.CloseOptions;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

@Slf4j
public abstract class KafkaTopic<T> implements AutoCloseable {

    protected final String topicName;
    protected final Class<T> payloadType;

    private final KafkaConsumer<String, String> consumer;
    private final List<T> recorded = new ArrayList<>();

    protected KafkaTopic(String topicName, Class<T> payloadType, String groupId) {
        this.topicName = topicName;
        this.payloadType = payloadType;
        this.consumer = new KafkaConsumer<>(KafkaConfig.consumerProps(groupId));
        consumer.subscribe(List.of(topicName));
        awaitAssignment(Duration.ofSeconds(15));

        Set<TopicPartition> partitions = consumer.assignment();
        consumer.seekToEnd(partitions);

        // Принудительно инициализируем позиции после seekToEnd.
        for (TopicPartition partition : partitions) {
            long position = consumer.position(partition);
            log.info("Kafka consumer initialized: topic={}, partition={}, position={}",
                    topicName, partition.partition(), position);
        }
    }

    private void awaitAssignment(Duration timeout) {
        long deadline = System.nanoTime() + timeout.toNanos();

        while (consumer.assignment().isEmpty()
                && System.nanoTime() < deadline) {
            consumer.poll(Duration.ofMillis(100));
        }

        if (consumer.assignment().isEmpty()) {
            throw new IllegalStateException(
                    ("Consumer не получил партиции топика %s за %s. "
                            + "Проверь bootstrap-servers и наличие топика.")
                            .formatted(topicName, timeout)
            );
        }

        log.info("Consumer получил assignment для {}: {}", topicName, consumer.assignment());
    }

    public T awaitMessage(String type, Predicate<T> filter, Duration timeout) {
        long deadline = System.nanoTime() + timeout.toNanos();

        while (System.nanoTime() < deadline) {
            ConsumerRecords<String, String> batch = consumer.poll(Duration.ofMillis(200));
            for (ConsumerRecord<String, String> record : batch) {
                log.info("Kafka record: topic={}, partition={}, offset={}, key={}, value={}",
                        record.topic(),
                        record.partition(),
                        record.offset(),
                        record.key(),
                        record.value());

                JsonNode envelope = Json.tree(record.value());

                if (!type.equals(envelope.path("type").asText())) {
                    continue;
                }

                T payload = parsePayload(envelope);
                recorded.add(payload);

                if (filter.test(payload)) {
                    log.info("Получено {} из {}: {}", type, topicName, payload);
                    return payload;
                }
            }
        }

        throw new AssertionError(
                "За " + timeout
                        + " не пришло сообщение type=" + type
                        + " в топик " + topicName
                        + ". Распарсенные сообщения: " + recorded
        );
    }

    public List<T> recorded() {
        return List.copyOf(recorded);
    }

    @Override
    public void close() {
        consumer.close(CloseOptions.timeout(Duration.ofSeconds(2)));
    }

    private T parsePayload(JsonNode envelope) {
        return Json.MAPPER.convertValue(
                envelope.path("payload"),
                payloadType
        );
    }
}
