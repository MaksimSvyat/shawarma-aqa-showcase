package com.shawarmashop.tests.extensions;

import com.shawarmashop.tests.integrations.IntegrationStubs;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.extension.*;

public class IntegrationStubsExtension implements BeforeEachCallback, AfterEachCallback, ParameterResolver {

    public static ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(IntegrationStubsExtension.class);
    public static String KEY = "partnerStubs";

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        IntegrationStubs stubs = context.getStore(NAMESPACE).get(KEY, IntegrationStubs.class);
        if (stubs != null) {
            stubs.resetToDefaults();
        }
    }

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        IntegrationStubs stubs = new IntegrationStubs();
        stubs.resetToDefaults();
        context.getStore(NAMESPACE).put(KEY, stubs);
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameterContext.getParameter().getType() == IntegrationStubs.class;
    }

    @Override
    public @Nullable Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return extensionContext.getStore(NAMESPACE).get(KEY, IntegrationStubs.class);
    }
}
