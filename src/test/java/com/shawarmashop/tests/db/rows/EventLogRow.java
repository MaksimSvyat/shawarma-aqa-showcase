package com.shawarmashop.tests.db.rows;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class EventLogRow {
    private Long id;
    private String type;
    private String payloadJson;
    private Instant ts;
}
