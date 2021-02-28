package org.jekajops.app.cnfg;

import org.apache.commons.exec.environment.EnvironmentUtils;
import org.jekajops.app.gui.GUI;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.IOException;
import java.util.Locale;
import java.util.prefs.Preferences;

public class AppConfig {
    public static final String EXEL_PATH_KEY = "EXEL_PATH";
    public static final Preferences preferences = Preferences.userRoot();
    public static final String NEW_PRICE_COL_NAME = "Проценка";
    public static final String DIFF_PRICES_COL_NAME = "Разница с проценкой";

    public static String getExelPath() {
        return preferences.get(EXEL_PATH_KEY, "");
    }

    public static WebDriver getWebDriver() {
        try {
//        System.setProperty("GOOGLE_CHROME_BIN", "/app/.apt/usr/bin/google-chrome");
//        System.setProperty("CHROMEDRIVER_PATH", "/app/.chromedriver/bin/chromedriver");
            ChromeOptions options = new ChromeOptions();
            if (getOS().contains("win")) {
                System.setProperty("webdriver.chrome.driver", System.getProperty("user.dir") + "/src/main/resources/chromedriver.exe");
            } else {
                //options.setBinary("/app/.apt/usr/bin/google-chrome");
                String binaryPath = null;
                try {
                    binaryPath = EnvironmentUtils.getProcEnvironment().get("GOOGLE_CHROME_SHIM");
                } catch (IOException e) {
                    e.printStackTrace();
                    log(e.getMessage());
                }
                log("Webdriver Binary Path: " + binaryPath);
                options.setBinary(binaryPath);
            }
            options.addArguments("--enable-javascript");
            //options.addArguments("--headless");
            options.addArguments("--disable-gpu");
            options.addArguments("--no-sandbox");
            return new ChromeDriver(options);
        } catch (Throwable t) {
            t.printStackTrace();
            log(t.getMessage());
        }
        return null;
    }

    public static String getOS() {
        return System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
    }

    private static void log(String msg) {
        GUI.log("AppConfig", msg);
    }

}
