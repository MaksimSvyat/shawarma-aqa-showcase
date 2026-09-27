package com.shawarmashop.tests.ui.extensions;

import com.codeborne.selenide.Configuration;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class SelenideProxyExtension implements BeforeEachCallback, AfterEachCallback {

    private boolean previousProxyState;

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        Configuration.proxyEnabled = previousProxyState;
    }

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        previousProxyState = Configuration.proxyEnabled;

        Configuration.proxyEnabled = true;
    }
}
