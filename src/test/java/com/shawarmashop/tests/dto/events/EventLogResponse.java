package com.shawarmashop.tests.dto.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventLogResponse {
    private Long id;
    private String type;
    private Map<String, Object> payload;
    private Instant ts;
}
