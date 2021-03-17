package com.github.evgenykuzin.app;

import com.github.evgenykuzin.app.gui.GUI;
import com.github.evgenykuzin.worker.StocksUpdater;
import com.jcabi.log.VerboseRunnable;
import com.github.evgenykuzin.app.cnfg.AppConfig;
import com.github.evgenykuzin.app.loger.GuiLogger;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.github.evgenykuzin.app.cnfg.AppConfig.logger;

public class AppGui {
    public static void main(String[] args) {
        AppConfig.setLogger(new GuiLogger());
        var stocksUpdater = new StocksUpdater();
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(4);
        GUI gui = new GUI();
        gui.setRunActionListener(e -> {
            logger.log("run");
            executorService.scheduleAtFixedRate(new VerboseRunnable(stocksUpdater, true), 0, 1, TimeUnit.MINUTES);
        });
        gui.start();
    }
}
