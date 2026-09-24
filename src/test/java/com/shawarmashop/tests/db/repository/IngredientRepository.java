package com.shawarmashop.tests.db.repository;

import com.shawarmashop.tests.db.core.Jdbc;
import com.shawarmashop.tests.db.core.RowMapper;
import com.shawarmashop.tests.db.rows.IngredientRow;
import com.shawarmashop.tests.db.core.TestDatabase;

import java.util.List;
import java.util.Optional;

public class IngredientRepository {

    private final Jdbc jdbc = TestDatabase.jdbc();

    static final RowMapper<IngredientRow> MAPPER = rs -> IngredientRow.builder()
            .id(rs.getLong("id"))
            .name(rs.getString("name"))
            .unit(rs.getString("unit"))
            .onHand(rs.getInt("on_hand"))
            .minLevel(rs.getInt("min_level"))
            .supplierCode(rs.getString("supplier_code"))
            .supplierChannel(rs.getString("supplier_channel"))
            .supplierLeadTimeDays(rs.getInt("supplier_lead_time_days"))
            .build();

    private static final String SELECT_ALL =
            "SELECT id, name, unit, on_hand, min_level, supplier_code, supplier_channel,"
                    + "       supplier_lead_time_days"
                    + "  FROM ingredients ";


    public Optional<IngredientRow> findById(long id) {
        return jdbc.queryForObject(SELECT_ALL + "WHERE id = ?", MAPPER, id);
    }

    public Optional<IngredientRow> findByName(String name) {
        return jdbc.queryForObject(SELECT_ALL + "WHERE name = ?", MAPPER, name);
    }

    public List<IngredientRow> findLowStock() {
        return jdbc.queryForList(
                SELECT_ALL + "WHERE on_hand <= min_level ORDER BY id", MAPPER);
    }
}
