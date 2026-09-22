package com.shawarmashop.tests.db.scope;

import com.shawarmashop.tests.db.core.Jdbc;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayDeque;
import java.util.Deque;

@Slf4j
@RequiredArgsConstructor
public class DbFixtureScope {

    private final Jdbc jdbc;
    private final Deque<Cleanup> tracked = new ArrayDeque<>();

    public void track(String description, Runnable cleanup) {
        tracked.push(new Cleanup(description, cleanup));
    }

    public void track(String table, long id) {
        track(table + " id=" + id,
                () -> jdbc.update(
                        "DELETE FROM " + table + " WHERE id = ?",
                        id
                )
        );
    }

    public void cleanup(){
        while (!tracked.isEmpty()) {
            Cleanup pop = tracked.pop();
            try {
                pop.getAction().run();
            } catch (RuntimeException e) {
                log.error("DbFixtureScope cleanup упал для {}\n{}", pop.getDescription(), e.getMessage());
            }
        }
    }

    public int size(){
        return tracked.size();
    }

}
