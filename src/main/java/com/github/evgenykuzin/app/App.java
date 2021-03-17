package com.github.evgenykuzin.app;

import com.github.evgenykuzin.core.util.cnfg.LogConfig;
import com.github.evgenykuzin.core.util.loger.BasicLogger;
import com.jcabi.log.VerboseRunnable;
import com.github.evgenykuzin.worker.PriceMonitor;

import java.util.concurrent.*;

import static com.github.evgenykuzin.core.util.cnfg.LogConfig.setLogger;

public class App {
    public static void main(String[] args) {
        setLogger(new BasicLogger());
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(2);
        LogConfig.logger.log("run PriceMonitor");
        executorService.scheduleAtFixedRate(new VerboseRunnable(new PriceMonitor(), true), 0, 5, TimeUnit.SECONDS);
    }
}
