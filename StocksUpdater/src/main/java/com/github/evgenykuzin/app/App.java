package com.github.evgenykuzin.app;

import com.github.evgenykuzin.app.loger.BasicLogger;
import com.github.evgenykuzin.worker.StocksUpdater;
import com.jcabi.log.VerboseRunnable;
import com.github.evgenykuzin.app.cnfg.AppConfig;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.github.evgenykuzin.app.cnfg.AppConfig.logger;

public class App {
    public static void main(String[] args) {
        AppConfig.setLogger(new BasicLogger());
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(4);
        logger.log("run StocksUpdater");
        executorService.scheduleAtFixedRate(new VerboseRunnable(new StocksUpdater(), true), 0, 1, TimeUnit.MINUTES);
    }
}
