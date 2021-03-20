package com.github.evgenykuzin.parser;

import com.github.evgenykuzin.core.entities.OzonProduct;
import com.github.evgenykuzin.core.entities.Product;
import com.github.evgenykuzin.core.util.SearchMatcher;
import com.github.evgenykuzin.core.util.loger.Loggable;
import com.github.evgenykuzin.core.util.managers.AntiCaptchaManager;
import com.github.evgenykuzin.core.util.managers.FileManager;
import net.lightbody.bmp.BrowserMobProxy;
import net.lightbody.bmp.BrowserMobProxyServer;
import net.lightbody.bmp.client.ClientUtil;
import org.apache.commons.exec.environment.EnvironmentUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated;

public class OzonSeleniumManager implements Loggable {
    private WebDriver webDriver;
    String url;

    public OzonSeleniumManager(String url) {
        this.url = url;
        initWebDriver();
        try {
            webDriver.get("https://www.ozon.ru/");
            System.out.println(webDriver.getPageSource());
            System.out.println(webDriver.findElement(By.tagName("iframe")).getAttribute("innerHTML"));
            System.out.println(webDriver.findElement(By.tagName("iframe")).getAttribute("outerHTML"));
            waitingToSolveCaptcha();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void search(int page, String key) {
        try {
            var url = getSearchUrl(page, key);
            webDriver.get(url);
            if (!webDriver.getCurrentUrl().equals(url)) {
                log("Warning: " + webDriver.getCurrentUrl() + " != " + url);
            }
        } catch (WebDriverException e) {
            e.printStackTrace();
            log(e.getMessage());
        }
    }

    public String parseProductsSKUByBarcode(String barcode, String nameForMatch, String brand) {
        String sku = null;
        if (barcode == null || barcode.isEmpty()) {
            log("Empty barcode search key!");
            return null;
        }
        try {
            List<WebElement> elements = getProductsElements(1, barcode, webDriver1 -> {
                try {
                    var brandSearhBy = By.xpath(".//span[@class='cy4']");
                    new WebDriverWait(webDriver1, 1).until(visibilityOfElementLocated(brandSearhBy));
                    webDriver1.findElements(brandSearhBy)
                            .get(0)
                            .click();
                    webDriver1.findElements(By.xpath(".//div[@class='b7n filter-block']"))
                            .get(0)
                            .findElements(By.tagName("input"))
                            .get(0)
                            .sendKeys(brand);
                    var checkbox = webDriver1.findElements(By.xpath(".//div[@class='cx8']"))
                            .get(0)
                            .findElements(By.tagName("a"))
                            .get(0)
                            .findElements(By.xpath(".//input[@class='_1V0q']"))
                            .get(0);
                    Actions act = new Actions(webDriver1);
                    act.moveToElement(checkbox).click().build().perform();
                } catch (WebDriverException | IndexOutOfBoundsException e) {
                    e.printStackTrace();
                }
            });
            System.out.println("elements = " + elements);
            if (!elements.isEmpty()) {
                var iam18By = By.xpath(".//div[@class='c8 _2avF']");
                var iam18btnBy = By.xpath(".//button[@class='_1-6r']");
                var iam18e = webDriver.findElements(iam18By);
                if (!iam18e.isEmpty()) iam18e.get(0).findElement(iam18btnBy).click();
                WebElement element = elements.get(0);
                int min = 1000;
                for (WebElement e : elements) {
                    var nameBy = By.xpath(".//a[@class='a2g0 tile-hover-target']");
                    int matchResult;
                    try {
                        var nameElement = e.findElement(nameBy);
                        matchResult = SearchMatcher.check(nameForMatch, nameElement.getText());
                    } catch (WebDriverException wde) {
                        continue;
                    }
                    if (matchResult < min) {
                        min = matchResult;
                        element = e;
                    }
                }
                List<WebElement> tileHoverTarget = new ArrayList<>();
                try {
                    tileHoverTarget = element.findElements(By.xpath(".//a[@class='a0v2 tile-hover-target']"));
                } catch (WebDriverException wde) {
                    wde.printStackTrace();
                }
                if (!tileHoverTarget.isEmpty()) {
                    var href = tileHoverTarget.get(0).getAttribute("href");
                    System.out.println("href = " + href);
                    if (href != null) {
                        var split = href.split("/");
                        System.out.println("split '/' = " + Arrays.toString(split));
                        if (split.length > 0) {
                            split = split[split.length - 1].split("-");
                            System.out.println("split '-' = " + Arrays.toString(split));
                            sku = split[split.length - 1].replaceAll("\\D", "");
                        }
                    }
                } else {
                    log("a0v2 tile-hover-target is empty");
                }
            } else {
                log("elements is empty");
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            log(e.getMessage());
        }
        return sku;
    }

    public List<Product> parseProductsPricesByBarcode(String barcode) {
        var products = new ArrayList<Product>();
        if (barcode == null || barcode.isEmpty()) {
            log("Empty barcode search key!");
            return products;
        }
        try {
            int page = 1;
            List<WebElement> elements;
            while (!(elements = getProductsElements(page, barcode)).isEmpty()) {
                var iam18By = By.xpath(".//div[@class='c8 _2avF']");
                var iam18btnBy = By.xpath(".//button[@class='_1-6r']");
                for (WebElement element : elements) {
                    var nameBy = By.xpath(".//a[@class='a2g0 tile-hover-target']");
                    var priceBy = By.xpath(".//div[@class='b5v4 a5d2 item']");
                    var iam18e = webDriver.findElements(iam18By);
                    if (!iam18e.isEmpty()) iam18e.get(0).findElement(iam18btnBy).click();
                    var nameElement = element.findElement(nameBy);
                    log("name = " + nameElement.getText());
                    var href = nameElement.getAttribute("href");
                    var price = element
                            .findElement(priceBy)
                            .findElement(By.tagName("span"))
                            .getText()
                            .replaceAll("\\D", "");
                    log("price = " + price);
                    try {
                        products.add(new OzonProduct(0, Double.parseDouble(price), nameElement.getText(), null, barcode, null, href, null, barcode, null, null));
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
                if (page > 10) break;
                page++;
                Thread.sleep(100 + new Random().nextInt(100));
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            log(e.getMessage());
        }
        return products;
    }

    private List<WebElement> getProductsElements(int page, String key, Consumer<WebDriver> filter) throws IOException, InterruptedException {
        waitingToSolveCaptcha();
        search(page, key);
        try {
            filter.accept(webDriver);
        } catch (Throwable t) {
            t.printStackTrace();
        }
        var bySearch = By.xpath("//div[@class='widget-search-result-container ao3']");
        List<WebElement> result = new ArrayList<>();
        try {
            var widgetSearchResultContainer = webDriver
                    .findElements(bySearch);
            if (widgetSearchResultContainer.isEmpty()) {
                log("Error: No class: widget-search-result-container");
                return result;
            }
            result = widgetSearchResultContainer
                    .get(0)
                    .findElements(By.xpath(".//div[@class='a0c6 a0c9 a0c8']"));
            if (result.isEmpty()) {
                result = widgetSearchResultContainer
                        .get(0)
                        .findElements(By.xpath(".//div[@class='a0c6 a0c9']"));
                if (result.isEmpty()) {
                    log("Error: No class: a0c6 a0c9 WebElements is empty");
                }
            }
        } catch (NoSuchElementException e) {
            e.printStackTrace();
            log(e.getMessage());
        }
        return result;
    }

    private List<WebElement> getProductsElements(int page, String key) throws IOException, InterruptedException {
        return getProductsElements(page, key, webDriver -> {
        });
    }

    private String solveCaptcha() throws InterruptedException {
        if (!webDriver.getPageSource().contains("ROBOTS")) return null;
        //var taskId = AntiCaptchaManager.createTask("6Ld38BkUAAAAAPATwit3FXvga1PI6iVTb6zgXw62", webDriver.getCurrentUrl());
        //Thread.sleep(20000);
        //return AntiCaptchaManager.getResult(taskId);
        return null;
    }

    private String getSearchUrl(int page, String key) {
        System.out.println("page URL = " + String.format(url, page, key));
        return String.format(url, page, key);
    }

    private void waitingToSolveCaptcha() {
        new WebDriverWait(webDriver, 1000000).until(webDriver -> {
            try {
                var hash = solveCaptcha();
                if (hash != null) {
                    webDriver.findElement(By.xpath("")).sendKeys(hash);
                    return true;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return !webDriver.getPageSource().contains("ROBOTS");
        });
    }

    public void initWebDriver() {
        webDriver = getWebDriver();
        if (webDriver == null) throw new NullPointerException("WebDriver == null");
    }

    public WebDriver getWebDriver() {
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
            options.setAcceptInsecureCerts(true);
            options.addArguments(String.format("user-data-dir=%s", FileManager.getFromResources("User Data").getAbsolutePath()));
            options.setPageLoadStrategy(PageLoadStrategy.EAGER);
            BrowserMobProxy proxy = setUpProxy();
            options.setProxy(ClientUtil.createSeleniumProxy(proxy));
            options.addExtensions(FileManager.getFromResources("anticaptcha-plugin_v0.52.crx"));
            //options.addExtensions(FileManager.getFromResources("ehemiojjcpldeipjhjkepfdaohajpbdo-1.6.6-Crx4Chrome.com.crx"));
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

    public void quit() {
        if (webDriver != null) webDriver.quit();
    }

    @Override
    protected void finalize() throws Throwable {
        quit();
    }
}
