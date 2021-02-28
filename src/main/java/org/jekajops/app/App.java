package org.jekajops.app;

import com.jcabi.log.VerboseRunnable;
import org.jekajops.app.gui.GUI;
import org.jekajops.worker.Worker;

import java.util.concurrent.*;

public class App {
    public static void main(String[] args) {
        Worker worker = new Worker();
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(4);
        GUI gui = new GUI();
        gui.setRunActionListener(e -> {
            GUI.log("run");
            executorService.scheduleAtFixedRate(new VerboseRunnable(worker, true), 0, 55, TimeUnit.SECONDS);
        });
        gui.start();
    }
}
