package com.shawarmashop.tests.kafka.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class StockEvent {
    private Long ingredientId;
    private String ingredientName;
    private String invoiceNumber;
    private String status;
    private String reason;
    private Integer onHand;
    private Integer minLevel;
}
