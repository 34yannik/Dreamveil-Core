package de.yannik.dreamveilCore.database;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DatabaseExecutor {

    private static final ExecutorService executor = Executors.newFixedThreadPool(4);

    public static void runAsync(Runnable task) {
        executor.execute(task);
    }

    public static void shutdown() {
        executor.shutdown();
    }
}