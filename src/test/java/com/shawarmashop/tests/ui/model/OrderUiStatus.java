package com.shawarmashop.tests.ui.model;

public enum OrderUiStatus {

    PENDING("PENDING"),
    PAID("PAID"),
    PREPARING("COOKING"),
    DONE("READY"),
    CANCELLED("CANCELLED");

    private final String label;

    OrderUiStatus(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
