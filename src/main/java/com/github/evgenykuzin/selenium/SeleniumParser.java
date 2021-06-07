package com.github.evgenykuzin.selenium;

import com.github.evgenykuzin.core.util_managers.FileManager;
import net.lightbody.bmp.BrowserMobProxy;
import net.lightbody.bmp.BrowserMobProxyServer;
import net.lightbody.bmp.client.ClientUtil;
import org.apache.commons.exec.environment.EnvironmentUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class SeleniumParser {
    ChromeDriver webDriver;

    public SeleniumParser() {
        webDriver = getChromeWebDriver();
    }

    public void get(String url) {
        webDriver.get(url);
    }

    public WebElement findElement(By by) {
        return findElement(by, webDriver);
    }

    public WebElement findElement(By by, SearchContext parent) {
        List<WebElement> elements = findElements(by, parent);
        if (elements != null && !elements.isEmpty()) {
            return elements.get(0);
        } else return null;
    }

    public List<WebElement> findElements(By by) {
        return findElements(by, webDriver);
    }

    public List<WebElement> findElements(By by, SearchContext parent) {
        return parent.findElements(by);
    }

    public ChromeDriver getWebDriver() {
        return webDriver;
    }

    protected static ChromeDriver getChromeWebDriver() {
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
                }
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
            options.addArguments(String.format("user-data-dir=%s", FileManager.getFromResources("User Data").getAbsolutePath()));
            options.setPageLoadStrategy(PageLoadStrategy.EAGER);
            options.addExtensions(FileManager.getFromResources("anticaptcha-plugin_v0.52.crx"));
            BrowserMobProxy proxy = setUpProxy();
            //options.setProxy(ClientUtil.createSeleniumProxy(proxy));
            options.merge(capabilities);
            return new ChromeDriver(options);
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return null;
    }

    protected static BrowserMobProxy setUpProxy() {
        BrowserMobProxyServer proxy = new BrowserMobProxyServer();
        //proxy.addHeader("User-Agent", "Chrome/87.0.4280.88");
        proxy.addHeader("User-Agent", "AdsBot-Google (+http://www.google.com/adsbot.html)");
        //proxy.setRequestTimeout(1000, TimeUnit.MILLISECONDS);
        proxy.start(0);
        return proxy;
    }

    protected static String getOS() {
        return System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
    }
}
