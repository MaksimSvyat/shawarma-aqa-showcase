package com.shawarmashop.tests.db.scope;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Cleanup {
    private String description;
    private Runnable action;
}
