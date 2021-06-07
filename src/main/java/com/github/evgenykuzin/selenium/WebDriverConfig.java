package com.github.evgenykuzin.selenium;

import com.github.evgenykuzin.core.util.loger.Loggable;
import com.github.evgenykuzin.core.util_managers.FileManager;
import lombok.Getter;
import lombok.Setter;
import net.lightbody.bmp.BrowserMobProxy;
import net.lightbody.bmp.BrowserMobProxyServer;
import net.lightbody.bmp.client.ClientUtil;
import org.apache.commons.exec.environment.EnvironmentUtils;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.IOException;
import java.util.Locale;

@Setter
@Getter
public abstract class WebDriverConfig implements Loggable {
    private String userDataPath;
    private String webDriverPath;

    //public abstract ChromeOptions initWebDriverOptions();

    public ChromeDriver getChromeWebDriver() {
        try {
            System.setProperty("GOOGLE_CHROME_BIN", "/app/.apt/usr/bin/google-chrome");
            System.setProperty("CHROMEDRIVER_PATH", "/app/.chromedriver/bin/chromedriver");
            ChromeOptions options = new ChromeOptions();
            if (getOS().contains("win")) {
                System.setProperty("webdriver.chrome.driver", FileManager.getFromResources("chromedriver.exe").getAbsolutePath());
            } else {
                //System.setProperty("webdriver.chrome.driver", EnvironmentUtils.getProcEnvironment().get("CHROMEDRIVER_PATH"));
                String binaryPath = null;
                try {
                    binaryPath = EnvironmentUtils.getProcEnvironment().get("CHROMEDRIVER_SHIM");
                } catch (IOException e) {
                    e.printStackTrace();
                    log(e.getMessage());
                }
                log("Webdriver Binary Path: " + binaryPath);
                options.setBinary(binaryPath);
            }
            DesiredCapabilities capabilities = DesiredCapabilities.chrome();
            options.addArguments("--enable-javascript");
            //options.addArguments("--headless");
            options.addArguments("--disable-gpu");
            options.addArguments("--no-sandbox");
            //options.addArguments("--disable-extensions");
            //options.addArguments("--disable-dev-shm-usage");
            options.setAcceptInsecureCerts(true);
            //options.addArguments("--disable-popup-blocking");
            options.addArguments(String.format("user-data-dir=%s", FileManager.getFromResources("User Data").getAbsolutePath()));
            options.setPageLoadStrategy(PageLoadStrategy.NORMAL);
            options.addExtensions(FileManager.getFromResources("anticaptcha-plugin_v0.52.crx"));
            BrowserMobProxy proxy = setUpProxy();
            options.setProxy(ClientUtil.createSeleniumProxy(proxy));
            options.merge(capabilities);
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

    private static BrowserMobProxy setUpProxy() {
        BrowserMobProxyServer proxy = new BrowserMobProxyServer();
        //proxy.addHeader("User-Agent", "Chrome/87.0.4280.88");
        proxy.addHeader("User-Agent", "AdsBot-Google (+http://www.google.com/adsbot.html)");
        //proxy.setRequestTimeout(1000, TimeUnit.MILLISECONDS);
        proxy.start(0);
        return proxy;
    }

}
