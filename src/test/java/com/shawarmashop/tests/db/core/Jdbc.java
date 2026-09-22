package com.shawarmashop.tests.db.core;

import lombok.RequiredArgsConstructor;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public final class Jdbc {
    private final DataSource dataSource;

    public  <T> Optional<T> queryForObject(String sql, RowMapper<T> mapper, Object... params) {
        List<T> ts = queryForList(sql, mapper, params);
        if (ts.isEmpty()) {
            return Optional.empty();
        }
        if (ts.size() > 1) {
            throw new IllegalStateException("Ожидалось максимум 1 строка, получено " + ts.size() + " для " + sql);
        }

        return Optional.of(ts.getFirst());
    }

    public  <T> List<T> queryForList(String sql, RowMapper<T> mapper, Object... params) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            bind(preparedStatement, params);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                List<T> result = new ArrayList<>();
                while (resultSet.next()) {
                    result.add(mapper.mapRow(resultSet));
                }
                return result;
            }
        } catch (SQLException e) {
            throw wrap(sql, e);
        }
    }

    public int update(String sql, Object... params) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            bind(preparedStatement, params);
            return preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw wrap(sql, e);
        }
    }

    public long insertReturningId(String sql, Object... params) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            bind(preparedStatement, params);
            preparedStatement.executeUpdate();
            try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getLong(1);
                }
                throw new IllegalStateException("INSERT не вернул сгенерированный ключ: " + sql);
            }
        } catch (SQLException e) {
            throw wrap(sql, e);
        }
    }

    private static void bind(PreparedStatement stmt, Object[] params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            stmt.setObject(i + 1, params[i]);
        }
    }


    private static RuntimeException wrap(String sql, SQLException e) {
        return new RuntimeException("JDBC ошибка на: " + sql + " - " + e.getMessage(), e);
    }
}

