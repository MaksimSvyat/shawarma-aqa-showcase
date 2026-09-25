package com.shawarmashop.tests.extensions;

import com.shawarmashop.tests.kafka.KafkaTestBus;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.extension.*;

public class KafkaTestBusExtension implements BeforeEachCallback, AfterEachCallback, ParameterResolver {

    public static ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(KafkaTestBusExtension.class);
    public static String KEY = "kafkaTestBus";

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        KafkaTestBus kafkaTestBus = context.getStore(NAMESPACE).get(KEY, KafkaTestBus.class);
        if (kafkaTestBus != null) {
            kafkaTestBus.close();
        }
    }

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        context.getStore(NAMESPACE).put(KEY, new KafkaTestBus());
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameterContext.getParameter().getType() == KafkaTestBus.class;
    }

    @Override
    public @Nullable Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return extensionContext.getStore(NAMESPACE).get(KEY, KafkaTestBus.class);
    }
}
