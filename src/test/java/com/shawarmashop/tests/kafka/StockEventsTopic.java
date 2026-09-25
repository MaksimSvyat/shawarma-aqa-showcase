package com.shawarmashop.tests.kafka;

import com.shawarmashop.tests.kafka.events.StockEvent;

import java.time.Duration;
import java.util.function.Predicate;

public class StockEventsTopic extends KafkaTopic<StockEvent> {

    public static final String TOPIC = "order.events";
    public static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(5);

    protected StockEventsTopic(String groupId) {
        super(TOPIC, StockEvent.class, groupId);
    }

    public StockEvent waitRestockPlaced(long ingredientId) {
        return waitRestockPlaced(ingredientId, DEFAULT_TIMEOUT);
    }

    public StockEvent waitRestockPlaced(long ingredientId, Duration timeout) {
        return awaitMessage("RESTOCK_PLACED", byIngredientId(ingredientId), timeout);
    }

    public StockEvent waitRestockFailed(long ingredientId) {
        return waitRestockFailed(ingredientId, DEFAULT_TIMEOUT);
    }

    public StockEvent waitRestockFailed(long ingredientId, Duration timeout) {
        return awaitMessage("RESTOCK_FAILED", byIngredientId(ingredientId), timeout);
    }

    public StockEvent waitStockLow(long ingredientId) {
        return awaitMessage("STOCK_LOW", byIngredientId(ingredientId), DEFAULT_TIMEOUT);
    }

    private static Predicate<StockEvent> byIngredientId(long id){
        return x->x.getIngredientId() != null && x.getIngredientId() == id;
    }
}
