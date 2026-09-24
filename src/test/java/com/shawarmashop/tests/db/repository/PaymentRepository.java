package com.shawarmashop.tests.db.repository;

import com.shawarmashop.tests.db.core.Jdbc;
import com.shawarmashop.tests.db.core.RowMapper;
import com.shawarmashop.tests.db.rows.PaymentRow;
import com.shawarmashop.tests.db.core.TestDatabase;

import java.util.List;
import java.util.Optional;

public class PaymentRepository {

    private final Jdbc jdbc = TestDatabase.jdbc();

    private static final RowMapper<PaymentRow> MAPPER = rs -> PaymentRow.builder()
            .id(rs.getLong("id"))
            .orderId(rs.getLong("order_id"))
            .idempotencyKey(rs.getString("idempotency_key"))
            .amount(rs.getBigDecimal("amount"))
            .method(rs.getString("method"))
            .txnId(rs.getString("txn_id"))
            .status(rs.getString("status"))
            .failureReason(rs.getString("failure_reason"))
            .createdAt(rs.getTimestamp("created_at").toInstant())
            .build();

    private static final String SELECT_ALL =
            "SELECT id, order_id, idempotency_key, amount, method, txn_id, status,"
                    + "       failure_reason, created_at"
                    + "  FROM payments ";


    public Optional<PaymentRow> findById(long id) {
        return jdbc.queryForObject(SELECT_ALL + "WHERE id = ?", MAPPER, id);
    }

    public Optional<PaymentRow> findByIdempotencyKey(String key) {
        return jdbc.queryForObject(SELECT_ALL + "WHERE idempotency_key = ?", MAPPER, key);
    }

    public Optional<PaymentRow> findByTxnId(String txnId) {
        return jdbc.queryForObject(SELECT_ALL + "WHERE txn_id = ?", MAPPER, txnId);
    }

    public List<PaymentRow> findByOrderId(long orderId) {
        return jdbc.queryForList(SELECT_ALL + "WHERE order_id = ? ORDER BY created_at", MAPPER, orderId);
    }
}
