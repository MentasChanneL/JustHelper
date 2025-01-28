package com.prikolz.justhelper.util;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Scheduler {
    public static void run(long mills, Runnable run) {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.schedule(() -> {
            run.run();
            scheduler.shutdown();
        }, mills, TimeUnit.MILLISECONDS);
    }
}
