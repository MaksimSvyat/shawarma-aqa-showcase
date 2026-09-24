package com.shawarmashop.tests.db.repository;

import com.shawarmashop.tests.db.core.Jdbc;
import com.shawarmashop.tests.db.core.RowMapper;
import com.shawarmashop.tests.db.core.TestDatabase;
import com.shawarmashop.tests.db.rows.EventLogRow;

import java.util.List;
import java.util.Optional;


public class EventLogRepository {

    private final Jdbc jdbc = TestDatabase.jdbc();

    private static final RowMapper<EventLogRow> MAPPER = rs -> EventLogRow.builder()
            .id(rs.getObject("id", Long.class))
            .type(rs.getString("type"))
            .payloadJson(rs.getString("payload"))
            .ts(rs.getTimestamp("ts").toInstant())
            .build();


    public Optional<EventLogRow> findById(long id) {
        return jdbc.queryForObject(
                "SELECT id, type, payload::text AS payload, ts FROM event_log WHERE id = ?",
                MAPPER, id);
    }

    public List<EventLogRow> findRecent(int limit) {
        return jdbc.queryForList(
                "SELECT id, type, payload::text AS payload, ts"
                        + "  FROM event_log ORDER BY ts DESC LIMIT ?",
                MAPPER, limit);
    }

    public List<EventLogRow> findByType(String type, int limit) {
        return jdbc.queryForList(
                "SELECT id, type, payload::text AS payload, ts"
                        + "  FROM event_log WHERE type = ? ORDER BY ts DESC LIMIT ?",
                MAPPER, type, limit);
    }
}
