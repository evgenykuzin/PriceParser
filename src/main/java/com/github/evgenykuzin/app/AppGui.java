package com.github.evgenykuzin.app;

import com.github.evgenykuzin.app.cnfg.AppConfig;
import com.github.evgenykuzin.app.gui.GUI;
import com.github.evgenykuzin.app.loger.GuiLogger;
import com.github.evgenykuzin.worker.PriceMonitor;
import com.jcabi.log.VerboseRunnable;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AppGui {
    public static void main(String[] args) {
        AppConfig.setLogger(new GuiLogger());
        PriceMonitor priceMonitor = new PriceMonitor();
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(4);
        GUI gui = new GUI();
        gui.setRunActionListener(e -> {
            AppConfig.logger.log("run");
            executorService.scheduleAtFixedRate(new VerboseRunnable(priceMonitor, true), 0, 55, TimeUnit.SECONDS);
        });
        gui.start();
    }
}
