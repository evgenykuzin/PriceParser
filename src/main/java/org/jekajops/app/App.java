package org.jekajops.app;

import com.jcabi.log.VerboseRunnable;
import org.jekajops.app.cnfg.AppConfig;
import org.jekajops.app.loger.BasicLogger;
import org.jekajops.worker.PriceMonitor;
import org.jekajops.worker.StocksUpdater;

import java.util.concurrent.*;

import static org.jekajops.app.cnfg.AppConfig.logger;

public class App {
    public static void main(String[] args) {
        AppConfig.setLogger(new BasicLogger());
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(4);
        logger.log("run");
        executorService.scheduleAtFixedRate(new VerboseRunnable(new PriceMonitor(), true), 0, 25, TimeUnit.SECONDS);
        executorService.scheduleAtFixedRate(new VerboseRunnable(new StocksUpdater(), true), 0, 1, TimeUnit.SECONDS);
    }
}
