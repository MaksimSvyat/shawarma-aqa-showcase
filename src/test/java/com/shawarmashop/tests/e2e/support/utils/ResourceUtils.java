package com.shawarmashop.tests.e2e.support.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Утилита для чтения бинарных файлов из ресурсов тестового проекта.
 * Используется для загрузки изображений через API.
 */
public final class ResourceUtils {

    private ResourceUtils() {
    }

    public static byte[] readResource(String path) {
        try {
            return Files.readAllBytes(Path.of(path));
        } catch (IOException e) {
            throw new RuntimeException("Не удалось прочитать файл: " + path, e);
        }
    }
}
