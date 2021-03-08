package org.jekajops.app;

import com.jcabi.log.VerboseRunnable;
import org.jekajops.app.cnfg.AppConfig;
import org.jekajops.app.gui.GUI;
import org.jekajops.app.loger.GuiLogger;
import org.jekajops.worker.PriceMonitor;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.jekajops.app.cnfg.AppConfig.logger;

public class AppGui {
    public static void main(String[] args) {
        AppConfig.setLogger(new GuiLogger());
        PriceMonitor priceMonitor = new PriceMonitor();
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(4);
        GUI gui = new GUI();
        gui.setRunActionListener(e -> {
            logger.log("run");
            executorService.scheduleAtFixedRate(new VerboseRunnable(priceMonitor, true), 0, 55, TimeUnit.SECONDS);
        });
        gui.start();
    }
}
