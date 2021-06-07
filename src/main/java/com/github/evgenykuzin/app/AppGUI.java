package com.github.evgenykuzin.app;

import com.github.evgenykuzin.core.cnfg.LogConfig;
import com.github.evgenykuzin.core.util.loger.BasicLogger;
import com.github.evgenykuzin.core.util_managers.FileManager;
import com.github.evgenykuzin.parser.YamarketParserSe;
import com.github.evgenykuzin.worker.PriceMonitor;
import com.jcabi.log.VerboseRunnable;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.github.evgenykuzin.core.cnfg.LogConfig.logger;
import static com.github.evgenykuzin.core.cnfg.LogConfig.setLogger;

public class AppGUI {
    private static final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(4);

    public static void main(String[] args) {
        var parantFile = new File("/");
        System.out.println((String.valueOf(parantFile)));
        JFrame frame = getFrame();
        frame.pack();
        JPanel mainPanel = getMainPanel();
        JTextArea logTextArea = new JTextArea();
        mainPanel.add(logTextArea);
        frame.add(mainPanel);
        setLogger(new BasicLogger());
        LogConfig.logger.log("run PriceMonitor");
        executorService.scheduleAtFixedRate(new VerboseRunnable(new PriceMonitor(), true), 0, 5, TimeUnit.SECONDS);
    }

    static JFrame getFrame() {
        JFrame frame = new JFrame();
        frame.setVisible(true);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setPreferredSize(new Dimension(400, 400));
        return frame;
    }

    static JPanel getMainPanel() {
        JPanel panel = new JPanel();
        panel.setPreferredSize(new Dimension(400, 400));
        JButton startYandexCardUpdaterBtn = new JButton("yandex update products");
        startYandexCardUpdaterBtn.addActionListener(e -> {
            var parantFile = new File("/");
            logger.log(String.valueOf(parantFile));
            executorService.execute(new VerboseRunnable(new YamarketParserSe()));
        });
        panel.add(startYandexCardUpdaterBtn);
        JButton startPriceMonitorBtn = new JButton("price monitoring");
        startPriceMonitorBtn.addActionListener(e -> {
            executorService.scheduleAtFixedRate(new VerboseRunnable(new PriceMonitor()), 0, 3, TimeUnit.MINUTES);
        });
        panel.add(startPriceMonitorBtn);
        return panel;
    }

    static void schedule() {

    }
}
