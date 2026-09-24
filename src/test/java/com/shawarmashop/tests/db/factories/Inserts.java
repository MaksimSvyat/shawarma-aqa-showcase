package com.shawarmashop.tests.db.factories;

import com.shawarmashop.tests.db.core.TestDatabase;
import com.shawarmashop.tests.db.scope.TestDbScope;
import com.shawarmashop.tests.db.repository.RecipeRepository;
import com.shawarmashop.tests.db.rows.RecipeRow;

import java.sql.Timestamp;
import java.time.Instant;

public class Inserts {

    public static RecipeRow recipe(RecipeFixture f) {
        Timestamp now = Timestamp.from(Instant.now());
        long id = TestDatabase.jdbc().insertReturningId(
                """
                              INSERT INTO recipes(
                              	name, description, size, price, prep_seconds, image_url, created_at, updated_at, created_by, updated_by)
                              	VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);
                        """, f.getName(), f.getDescription(), f.getSize(), f.getPrice(),
                f.getPrepSeconds(), f.getImageUrl(), now, now, "owner", "owner");

        TestDbScope.current().track("recipes", id);
        return new RecipeRepository().findById(id).orElseThrow();
    }

    public static long recipeIngredient(long recipeId, long ingredientId, int qtyNeeded) {
        long id = TestDatabase.jdbc().insertReturningId(
                "INSERT INTO recipe_ingredients (recipe_id, ingredient_id, qty_needed)"
                        + " VALUES (?, ?, ?)",
                recipeId, ingredientId, qtyNeeded);
        TestDbScope.current().track("recipe_ingredients", id);
        return id;
    }
}
