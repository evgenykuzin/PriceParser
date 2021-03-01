package org.jekajops.app.cnfg;

import net.lightbody.bmp.BrowserMobProxy;
import net.lightbody.bmp.BrowserMobProxyServer;
import net.lightbody.bmp.client.ClientUtil;
import org.apache.commons.exec.environment.EnvironmentUtils;
import org.jekajops.app.loger.Loger;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Locale;
import java.util.prefs.Preferences;

public class AppConfig {
    public static final String EXEL_PATH_KEY = "EXEL_PATH";
    public static final Preferences preferences = Preferences.userRoot();
    public static final String NEW_PRICE_COL_NAME = "Проценка";
    public static final String DIFF_PRICES_COL_NAME = "Разница с проценкой";
    public static Loger loger;

    public static String getExelPath() {
        return preferences.get(EXEL_PATH_KEY, "");
    }

    public static void setLoger(Loger Loger1) {
        loger = Loger1;
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
            options.setAcceptInsecureCerts(true);
            //options.addArguments("user-data-dir=C:/Users/jekajops/AppData/Local/Google/Chrome/User Data");
            //options.setPageLoadStrategy(PageLoadStrategy.NORMAL);
            BrowserMobProxy proxy = setUpProxy();
            options.setProxy(ClientUtil.createSeleniumProxy(proxy));
            var webDriver = new ChromeDriver(options);
            return webDriver;
        } catch (Throwable t) {
            t.printStackTrace();
            log(t.getMessage());
        }
        return null;
    }

    private static BrowserMobProxy setUpProxy() {
        BrowserMobProxyServer proxy = new BrowserMobProxyServer();
        proxy.addHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.95 Safari/537.36");
        proxy.start(0);
        return proxy;
    }

    public static String getOS() {
        return System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
    }

    private static void log(String msg) {
        if (loger != null) loger.log("AppConfig", msg);
    }

}
