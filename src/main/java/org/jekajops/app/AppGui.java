package org.jekajops.app;

import com.jcabi.log.VerboseRunnable;
import org.jekajops.app.cnfg.AppConfig;
import org.jekajops.app.gui.GUI;
import org.jekajops.app.loger.GuiLoger;
import org.jekajops.worker.Worker;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.jekajops.app.cnfg.AppConfig.loger;

public class AppGui {
    public static void main(String[] args) {
        AppConfig.setLoger(new GuiLoger());
        Worker worker = new Worker();
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(4);
        GUI gui = new GUI();
        gui.setRunActionListener(e -> {
            loger.log("run");
            executorService.scheduleAtFixedRate(new VerboseRunnable(worker, true), 0, 55, TimeUnit.SECONDS);
        });
        gui.start();
    }
}
