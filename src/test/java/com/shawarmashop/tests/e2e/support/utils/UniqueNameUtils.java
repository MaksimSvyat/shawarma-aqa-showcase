package com.shawarmashop.tests.e2e.support.utils;

import java.util.UUID;

/**
 * Утилита для генерации уникальных строк.
 * Используется для создания названий рецептов, ингредиентов и других сущностей,
 * чтобы избежать конфликтов между параллельными тестами.
 */
public final class UniqueNameUtils {

    private UniqueNameUtils() {
    }

    public static String uniqueName(String prefix) {
        return "E2E_" + prefix + "_" + UUID.randomUUID();
    }
}
