package com.shawarmashop.tests.db.core;

public class TestDatabase {
    private final static class Holder {
        private final static Jdbc JDBC = new Jdbc(HikariPool.dataSource());
    }

    public static Jdbc jdbc(){
        return Holder.JDBC;
    }
}
