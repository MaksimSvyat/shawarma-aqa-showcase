package com.shawarmashop.tests.kafka;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
public final class KafkaTestBus implements AutoCloseable {

    private final String groupId = "tests-" + UUID.randomUUID();

    private OrderEventsTopic orders;
    private StockEventsTopic stock;

    public OrderEventsTopic orders() {
        if (orders == null) {
            orders = new OrderEventsTopic(groupId);
        }
        return orders;
    }

    public StockEventsTopic stock() {
        if (stock == null) {
            stock = new StockEventsTopic(groupId);
        }
        return stock;
    }

    @Override
    public void close() {
        for (KafkaTopic<?> topic : initialised()) {
            try {
                topic.close();
            } catch (Exception e) {
                log.error("Ошибка при закрытии топика: {}", e.getMessage());
            }
        }
    }

    private List<KafkaTopic<?>> initialised() {
        List<KafkaTopic<?>> list = new ArrayList<>(2);

        if (orders != null) {
            list.add(orders);
        }

        if (stock != null) {
            list.add(stock);
        }

        return list;
    }
}
