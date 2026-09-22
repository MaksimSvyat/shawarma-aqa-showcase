package com.shawarmashop.tests.db.scope;

import com.shawarmashop.tests.db.core.TestDatabase;

public class TestDbScope {
    private final static ThreadLocal<DbFixtureScope> SCOPE =
            ThreadLocal.withInitial(() -> new DbFixtureScope(TestDatabase.jdbc()));

    public static DbFixtureScope current(){
        return SCOPE.get();
    }

    public static void cleanup(){
        SCOPE.get().cleanup();
        SCOPE.remove();
    }
}
