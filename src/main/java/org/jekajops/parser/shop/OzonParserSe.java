package org.jekajops.parser.shop;

import org.jekajops.app.cnfg.AppConfig;
import org.jekajops.entities.OzonProduct;
import org.jekajops.entities.Product;
import org.openqa.selenium.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.jekajops.app.cnfg.AppConfig.logger;

public class OzonParserSe implements ShopParser {
    private static final String URL = "https://www.ozon.ru/search/?from_global=true&page=%d&text=%s";
    private WebDriver webDriver;

    public OzonParserSe() {
        initWebDriver();
    }

    @Override
    public List<Product> parseProducts(String key) {
        var products = new ArrayList<Product>();
        try {
            int page = 1;
            List<WebElement> elements;
            while (!(elements = getProductsElements(page, key)).isEmpty()) {
                log("elements = " + elements);
                Thread.sleep(30*1000+new Random().nextInt(60*1000));
                for (WebElement element : elements) {
                    Thread.sleep(new Random().nextInt(1000));
                    var name = element
                            .findElement(By.xpath(".//a[@class='a2g0 tile-hover-target']"))
                            .getText();
                    log("name = " + name);
                    Thread.sleep(new Random().nextInt(1000));
                    var price = element
                            .findElement(By.xpath(".//div[@class='b5v4 a5d2 item']"))
                            .findElement(By.tagName("span"))
                            .getText()
                            .replaceAll("\\D", "");
                    log("price = " + price);
                    products.add(new OzonProduct(0, Double.parseDouble(price), name, key, null));
                }
                if (page > 10) break;
                page++;
                break;
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
                log("Error: " + webDriver.getCurrentUrl() + " != " + url);
            }
        } catch (WebDriverException e) {
            e.printStackTrace();
            log(e.getMessage());
        }
    }

    private List<WebElement> getProductsElements(int page, String key) throws IOException, InterruptedException {
        webDriver.get("https://193.232.37.91/");
        Thread.sleep(new Random().nextInt(1000));
        webDriver.get("https://www.ozon.ru/");
        Thread.sleep(new Random().nextInt(1000));
        search(page, key);
        List<WebElement> result = new ArrayList<>();
        try {
            Thread.sleep(new Random().nextInt(1000));
            var widgetSearchResultContainer = webDriver
                    .findElements(By.xpath("//div[@class='widget-search-result-container ao3']"));
            if (widgetSearchResultContainer.isEmpty()) {
                log("Error: No class: widget-search-result-container");
                log(webDriver.getCurrentUrl());
                log(webDriver.getPageSource());
                return result;
            }
            Thread.sleep(new Random().nextInt(1000));
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
        System.out.println("page = " + String.format(URL, page, key));
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
