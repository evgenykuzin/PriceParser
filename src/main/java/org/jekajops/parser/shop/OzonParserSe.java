package org.jekajops.parser.shop;

import org.jekajops.app.cnfg.AppConfig;
import org.jekajops.entities.OzonProduct;
import org.jekajops.entities.Product;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.jekajops.app.cnfg.AppConfig.logger;
import static org.openqa.selenium.support.ui.ExpectedConditions.*;

public class OzonParserSe implements ShopParser {
    private static final String URL = "https://www.ozon.ru/category/tovary-dlya-vzroslyh-9000/?page=%d&text=%s";
    private WebDriver webDriver;
    private WebDriverWait wait;
    public OzonParserSe() {
        initWebDriver();
        wait = new WebDriverWait(webDriver, 54321);
        try {
            webDriver.get("https://www.ozon.ru/");
            wait.until(webDriver -> !webDriver.getPageSource().contains("ROBOTS"));
        } catch (Throwable e) {e.printStackTrace();}
    }

    @Override
    public List<Product> parseProducts(String barcode) {
        var products = new ArrayList<Product>();
        if (barcode == null || barcode.isEmpty()) {
            log("Empty barcode search key!");
            return products;
        }
        try {
            int page = 1;
            List<WebElement> elements;
            while (!(elements = getProductsElements(page, barcode)).isEmpty()) {
                log("elements = " + elements);
                for (WebElement element : elements) {
                    var nameElement = element
                            .findElement(By.xpath(".//a[@class='a2g0 tile-hover-target']"));
                    log("name = " + nameElement.getText());
                    var href = nameElement.getAttribute("href");
                    var price = element
                            .findElement(By.xpath(".//div[@class='b5v4 a5d2 item']"))
                            .findElement(By.tagName("span"))
                            .getText()
                            .replaceAll("\\D", "");
                    log("price = " + price);
                    products.add(new OzonProduct(0, Double.parseDouble(price), nameElement.getText(), barcode, null, href, null, barcode));
                }
                if (page > 10) break;
                page++;
                Thread.sleep(1000+new Random().nextInt(1000));
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            log(e.getMessage());
        }
        return products;
    }

    private void search(int page, String key) {
        try {
            var url = getUrl(page, key);
            webDriver.get(url);
            if (!webDriver.getCurrentUrl().equals(url)) {
                log("Warning: " + webDriver.getCurrentUrl() + " != " + url);
            }
        } catch (WebDriverException e) {
            e.printStackTrace();
            log(e.getMessage());
        }
    }

    private List<WebElement> getProductsElements(int page, String key) throws IOException, InterruptedException {
        wait.until(webDriver -> !webDriver.getPageSource().contains("ROBOTS"));
        search(page, key);
        var bySearch = By.xpath("//div[@class='widget-search-result-container ao3']");
        List<WebElement> result = new ArrayList<>();
        try {
            var widgetSearchResultContainer = webDriver
                    .findElements(bySearch);
            if (widgetSearchResultContainer.isEmpty()) {
                log("Error: No class: widget-search-result-container");
                return result;
            }
            Thread.sleep(new Random().nextInt(100));
            result = widgetSearchResultContainer
                    .get(0)
                    .findElements(By.xpath(".//div[@class='a0c6 a0c9 a0c8']"));
            if (result.isEmpty()) log("Error: No class: a0c6 a0c9 a0c8\nWebElements is empty\n\n" + webDriver.getPageSource());
        } catch (NoSuchElementException e) {
            e.printStackTrace();
            log(e.getMessage());
        }
        return result;
    }

    private static String getUrl(int page, String key) {
        System.out.println("page URL = " + String.format(URL, page, key));
        return String.format(URL, page, key);
    }

    public void initWebDriver() {
        webDriver = AppConfig.getWebDriver();
    }

    public void quit() {
        if (webDriver != null) webDriver.quit();
    }

    private void log(String msg) {
        logger.log(getClass().getName(), msg);
    }

    @Override
    protected void finalize() throws Throwable {
        quit();
    }

}
