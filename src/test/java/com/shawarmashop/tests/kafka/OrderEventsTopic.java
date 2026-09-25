package com.shawarmashop.tests.kafka;

import com.shawarmashop.tests.kafka.events.OrderEvent;

import java.time.Duration;
import java.util.function.Predicate;

public final class OrderEventsTopic extends KafkaTopic<OrderEvent>{

    public static final String TOPIC = "order.events";
    public static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(15);

    public OrderEventsTopic(String groupId) {
        super(TOPIC, OrderEvent.class, groupId);
    }

    public OrderEvent waitPlaced(long orderId) {
        return waitPlaced(orderId, DEFAULT_TIMEOUT);
    }

    public OrderEvent waitPlaced(long orderId, Duration timeout) {
        return awaitMessage("ORDER_PLACED", byOrderId(orderId), timeout);
    }

    public OrderEvent waitPaid(long orderId) {
        return waitPaid(orderId, DEFAULT_TIMEOUT);
    }

    public OrderEvent waitPaid(long orderId, Duration timeout) {
        return awaitMessage("ORDER_PAID", byOrderId(orderId), timeout);
    }

    public OrderEvent waitCancelled(long orderId) {
        return awaitMessage("ORDER_CANCELLED", byOrderId(orderId), DEFAULT_TIMEOUT);
    }

    public OrderEvent waitDone(long orderId) {
        return waitDone(orderId, DEFAULT_TIMEOUT);
    }

    public OrderEvent waitDone(long orderId, Duration timeout) {
        return awaitMessage("ORDER_DONE", byOrderId(orderId), timeout);
    }

    public OrderEvent waitBreadReservationFailed(long orderId) {
        return awaitMessage("BREAD_RESERVATION_FAILED", byOrderId(orderId), DEFAULT_TIMEOUT);
    }

    private static Predicate<OrderEvent> byOrderId(long orderId) {
        return e -> e.getOrderId() != null && e.getOrderId() == orderId;
    }
}
