package com.github.evgenykuzin.app;

import com.jcabi.log.VerboseRunnable;
import com.github.evgenykuzin.app.cnfg.AppConfig;
import com.github.evgenykuzin.app.loger.BasicLogger;
import com.github.evgenykuzin.worker.PriceMonitor;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.github.evgenykuzin.app.cnfg.AppConfig.logger;

public class App {
    public static void main(String[] args) {
        AppConfig.setLogger(new BasicLogger());
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(4);
        logger.log("run PriceMonitor");
        executorService.scheduleAtFixedRate(new VerboseRunnable(new PriceMonitor(), true), 0, 25, TimeUnit.SECONDS);
    }
}
