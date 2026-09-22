package com.shawarmashop.tests.extensions;

import com.shawarmashop.tests.db.scope.TestDbScope;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class DbScopeExtension implements AfterEachCallback {
    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        TestDbScope.cleanup();
    }
}
