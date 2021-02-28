package org.jekajops.parser.shop;

import org.jekajops.app.cnfg.AppConfig;
import org.jekajops.entities.OzonProduct;
import org.jekajops.entities.Product;
import org.openqa.selenium.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.List;

import static org.jekajops.app.cnfg.AppConfig.loger;

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
                System.out.println("elements = " + elements);
                for (WebElement element : elements) {
                    var name = element
                            .findElement(By.xpath(".//a[@class='a2g0 tile-hover-target']"))
                            .getText();
                    System.out.println("name = " + name);
                    var price = element
                            .findElement(By.xpath(".//div[@class='b5v4 a5d2 item']"))
                            .findElement(By.tagName("span"))
                            .getText()
                            .replaceAll("\\D", "");
                    System.out.println("price = " + price);
                    products.add(new OzonProduct(0, Double.parseDouble(price), name, key, null));
                }
                if (page > 10) break;
                page++;
            }
        } catch (IOException e) {
            e.printStackTrace();
            log(e.getMessage());
        }
        return products;
    }

    private void getDocument(int page, String key) {
        Proxy proxy = new Proxy(
                Proxy.Type.HTTP,
                InetSocketAddress.createUnresolved("146.66.172.217", 8080)
        );
        try {
            webDriver.get(getUrl(page, key));
        } catch (WebDriverException e) {
            e.printStackTrace();
            log(e.getMessage());
        }
    }

    private List<WebElement> getProductsElements(int page, String key) throws IOException {
        getDocument(page, key);
        List<WebElement> result = new ArrayList<>();
        try {
            var widgetSearchResultContainer = webDriver
                    .findElements(By.xpath("//div[@class='widget-search-result-container ao3']"));
            if (widgetSearchResultContainer.isEmpty()) return result;
            result = widgetSearchResultContainer
                    .get(0)
                    .findElements(By.xpath(".//div[@class='a0c6 a0c9 a0c8']"));
            System.out.println("result = " + result);
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

    protected WebElement findElementByXpath(String xpath) {
        return webDriver.findElement(By.xpath(xpath));
    }

    protected WebElement findElementBy(By by) {
        return webDriver.findElement(by);
    }

    void cleanText(WebElement element, int n) {
        element.sendKeys("\b".repeat(n));
    }

    public void quit() {
        if (webDriver != null) webDriver.quit();
    }

    private void log(String msg) {
        loger.log(getClass().getName(), msg);
    }

    @Override
    protected void finalize() throws Throwable {
        quit();
    }
}
