package org.jekajops.app.cnfg;

import net.lightbody.bmp.BrowserMobProxy;
import net.lightbody.bmp.BrowserMobProxyServer;
import net.lightbody.bmp.client.ClientUtil;
import org.apache.commons.exec.environment.EnvironmentUtils;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jekajops.app.loger.Logger;
import org.jekajops.util.FileManager;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.IOException;
import java.util.Locale;
import java.util.prefs.Preferences;

public class AppConfig {
    public static final Preferences preferences = Preferences.userRoot();
    public static final String EXEL_PATH_KEY = "EXEL_PATH";

    public static final String AWS_ACCESS_KEY_ID = "AKIAIGEJPB3F4UUDYMOA";
    public static final String AWS_SECRET_ACCESS_KEY = "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\\n";
    public static final String AWS_PASS = "Ktoyatakoy21!";

    public static Logger logger;

    public static String getExelPath() {
        return preferences.get(EXEL_PATH_KEY, "");
    }

    public static void setLogger(Logger Logger1) {
        logger = Logger1;
    }

    public static WebDriver getWebDriver() {
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
                    binaryPath = EnvironmentUtils.getProcEnvironment().get("GOOGLE_CHROME_SHIM");
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
            options.addArguments("--disable-dev-shm-usage");
            options.setAcceptInsecureCerts(true);
            options.addArguments(String.format("user-data-dir=%s", FileManager.getFromResources("User Data").getAbsolutePath()));
            options.setPageLoadStrategy(PageLoadStrategy.EAGER);
            BrowserMobProxy proxy = setUpProxy();
            options.setProxy(ClientUtil.createSeleniumProxy(proxy));
            options.addExtensions(FileManager.getFromResources("anticaptcha-plugin_v0.52.crx"));
            options.merge(capabilities);
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
        //proxy.addHeader("User-Agent", "Chrome/87.0.4280.88");
        proxy.addHeader("User-Agent", "AdsBot-Google (+http://www.google.com/adsbot.html)");
        //proxy.setRequestTimeout(1000, TimeUnit.MILLISECONDS);
        proxy.start(0);
        return proxy;
    }

    public static String getOS() {
        return System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
    }

    private static void log(String msg) {
        if (logger != null) logger.log("AppConfig", msg);
    }

    public static void main(String[] args) throws IOException {
        System.out.printf("docker run -e COMMANDER_PASSWORD='%s' \\\n" +
                "    -e PROVIDERS_AWSEC2_ACCESSKEYID='%s' \\\n" +
                "    -e PROVIDERS_AWSEC2_SECRETACCESSKEY='%s' \\\n" +
                "    -it -p 8888:8888 -p 8889:8889 fabienvauchelles/scrapoxy\n",
                AWS_PASS, AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY);
        var c = HttpClientBuilder.create().build();
        var req = RequestBuilder.get("http://localhost:8889/api/instances").build();
        var res = c.execute(req);
        System.out.println("res = " + res);
    }

}
