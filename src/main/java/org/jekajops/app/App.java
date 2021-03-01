package org.jekajops.app;

import org.jekajops.app.cnfg.AppConfig;
import org.jekajops.app.loger.BasicLogger;
import org.jekajops.worker.Worker;

import java.util.concurrent.*;

import static org.jekajops.app.cnfg.AppConfig.logger;

public class App {
    public static void main(String[] args) {
        AppConfig.setLogger(new BasicLogger());
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(4);
        logger.log("run");
        while (true) {
            new Worker().runTask();
        }
        //executorService.scheduleAtFixedRate(new VerboseRunnable(new Worker(), true), 0, 25, TimeUnit.SECONDS);
    }
}
