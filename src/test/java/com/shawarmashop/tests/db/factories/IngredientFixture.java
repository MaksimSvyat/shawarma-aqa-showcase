package com.shawarmashop.tests.db.factories;

import com.shawarmashop.tests.db.core.Jdbc;
import com.shawarmashop.tests.db.core.TestDatabase;
import com.shawarmashop.tests.db.scope.TestDbScope;
import com.shawarmashop.tests.db.repository.IngredientRepository;
import com.shawarmashop.tests.db.rows.IngredientRow;
import lombok.Builder;
import lombok.Data;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class IngredientFixture {

    @Builder.Default
    private String name = "TEST_INGREDIENT_" + UUID.randomUUID();

    @Builder.Default
    private String unit = "g";

    @Builder.Default
    private int onHand = 1000;

    @Builder.Default
    private int minLevel = 100;

    @Builder.Default
    private String supplierCode = "TEST_SUP_" + UUID.randomUUID().toString().substring(0, 8);

    @Builder.Default
    private String supplierChannel = "REST";

    @Builder.Default
    private int supplierLeadTimeDays = 1;

    public IngredientRow insert() {
        return ingredient(this);
    }

    private IngredientRow ingredient(IngredientFixture fixture) {
        Jdbc jdbc = TestDatabase.jdbc();
        Timestamp now = Timestamp.from(Instant.now());

        long id = jdbc.insertReturningId(
                """
                INSERT INTO ingredients (
                    name,
                    unit,
                    on_hand,
                    min_level,
                    supplier_code,
                    supplier_channel,
                    supplier_lead_time_days,
                    created_at,
                    updated_at,
                    created_by,
                    updated_by
                )
                VALUES (?, ?, ?, ?, ?, ?::varchar, ?, ?, ?, ?, ?)
                """,
                fixture.getName(),
                fixture.getUnit(),
                fixture.getOnHand(),
                fixture.getMinLevel(),
                fixture.getSupplierCode(),
                fixture.getSupplierChannel(),
                fixture.getSupplierLeadTimeDays(),
                now,
                now,
                "tests",
                "tests"
        );

        TestDbScope.current().track(
                "ingredient aggregate id=" + id,
                () -> {
                    // Создаётся backend во время restock.
                    jdbc.update(
                            "DELETE FROM restock_invoices WHERE ingredient_id = ?",
                            id
                    );

                    jdbc.update(
                            "DELETE FROM recipe_ingredients WHERE ingredient_id = ?",
                            id
                    );

                    jdbc.update(
                            "DELETE FROM ingredients WHERE id = ?",
                            id
                    );
                }
        );

        return new IngredientRepository().findById(id).orElseThrow();
    }
}
