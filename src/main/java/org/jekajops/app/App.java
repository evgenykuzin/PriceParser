package org.jekajops.app;

import com.jcabi.log.VerboseRunnable;
import org.jekajops.app.cnfg.AppConfig;
import org.jekajops.app.loger.BasicLoger;
import org.jekajops.worker.Worker;

import java.util.concurrent.*;

import static org.jekajops.app.cnfg.AppConfig.loger;

public class App {
    public static void main(String[] args) {
        AppConfig.setLoger(new BasicLoger());
        Worker worker = new Worker();
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(4);
        loger.log("run");
        executorService.scheduleAtFixedRate(new VerboseRunnable(worker, true), 0, 25, TimeUnit.SECONDS);
    }
}
