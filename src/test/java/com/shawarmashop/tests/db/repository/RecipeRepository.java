package com.shawarmashop.tests.db.repository;

import com.shawarmashop.tests.db.core.Jdbc;
import com.shawarmashop.tests.db.core.RowMapper;
import com.shawarmashop.tests.db.core.TestDatabase;
import com.shawarmashop.tests.db.rows.RecipeRow;

import java.util.List;
import java.util.Optional;

public class RecipeRepository {

    private final Jdbc jdbc = TestDatabase.jdbc();

    static final RowMapper<RecipeRow> MAPPER = rs -> RecipeRow.builder()
            .id(rs.getInt("id"))
            .name(rs.getString("name"))
            .description(rs.getString("description"))
            .size(rs.getString("size"))
            .price(rs.getDouble("price"))
            .prepSeconds(rs.getInt("prep_seconds"))
            .imageUrl(rs.getString("image_url"))
            .build();

    private static final String SELECT_ALL =
            "SELECT id, name, description, size, price, prep_seconds, image_url"
                    + "  FROM recipes ";

    public Optional<RecipeRow> findById(long id) {
        return jdbc.queryForObject(SELECT_ALL + "WHERE id = ?", MAPPER, id);
    }

    public Optional<RecipeRow> findByName(String name) {
        return jdbc.queryForObject(SELECT_ALL + "WHERE name = ?", MAPPER, name);
    }

    public List<RecipeRow> findAll() {
        return jdbc.queryForList(SELECT_ALL + "ORDER BY id", MAPPER);
    }

    public long count() {
        return jdbc.queryForObject(
                "SELECT COUNT(*) AS c FROM recipes",
                rs -> rs.getLong("c")).orElse(0L);
    }
}
