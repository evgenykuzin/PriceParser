package com.github.evgenykuzin.parser;

import com.github.evgenykuzin.core.data_managers.WebCsvDataManager;
import com.github.evgenykuzin.core.entities.Product;
import com.github.evgenykuzin.core.entities.Table;
import com.github.evgenykuzin.core.util.cnfg.TableConfig;
import com.github.evgenykuzin.core.util.managers.FileManager;
import org.apache.commons.exec.environment.EnvironmentUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.github.evgenykuzin.core.util.cnfg.TableConfig.AdditionalOzonDocFieldsConfig.SUPPLIER_COL_NAME;
import static com.github.evgenykuzin.core.util.cnfg.WebDriverConfig.getOS;


public class SexTrgParser implements SupplierParser {
    private static final String URL = "http://sexopttorg.ru/";
    private static final String SEARCH_PARAM = "?search=%s";
    private final WebDriver webDriver;

    public SexTrgParser() {
        webDriver = getWebDriver();
        if (webDriver != null) {
            webDriver.get(URL);
            webDriver.findElements(By.xpath(".//input[@name='login']")).get(0).sendKeys("sellermp@ya.ru");
            webDriver.findElements(By.xpath(".//input[@name='psw']")).get(0).sendKeys("45627896");
            webDriver.findElements(By.xpath(".//input[@name='submit_enter']")).get(0).click();
        }
    }

    public List<Product> parseNewStocksProducts(List<Product> products) {
        var resultProducts = new ArrayList<Product>();
        if (webDriver == null) return resultProducts;
        products.forEach(product -> {
                var stock = parseNewStock(product.getArticle());
                stock = stock.replaceAll("\\D", "");
            if (stock.isEmpty()) stock = "0";
            var intStck = Integer.parseInt(stock);
            intStck = intStck > 0 ? intStck - 1 : 0;
            product.setStock(intStck);
            resultProducts.add(product);
        });
        return resultProducts;
    }

    public String parseNewStock(String searchKey) {
        webDriver.get(URL+String.format(SEARCH_PARAM, searchKey));
        var var1 = safeFind(webDriver, By.className("tablesorter"), 0);
        var var2 = safeFind(var1, By.tagName("tbody"),0);
        var var3 = safeFind(var2, By.tagName("tr"),0);
        var var4 = safeFind(var3, By.tagName("td"),4);
        if (var4 == null) return "";
        return var4.getText();
    }

    private static WebElement safeFind(@Nullable SearchContext element, By by, int i) {
        if (element == null) return null;
        var f = element.findElements(by);
        if (f.isEmpty()) return null;
        return f.get(i);
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
                    binaryPath = EnvironmentUtils.getProcEnvironment().get("GOOGLE_CHROME_BIN");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                options.setBinary(binaryPath);
            }
            DesiredCapabilities capabilities = DesiredCapabilities.chrome();
            options.addArguments("--enable-javascript");
            options.addArguments("--headless");
            options.addArguments("--disable-gpu");
            options.addArguments("--no-sandbox");
            options.setAcceptInsecureCerts(true);
            options.merge(capabilities);
            return new ChromeDriver(options);
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return null;
    }

    public static void updateSuppliers(WebCsvDataManager dataManager) {
        Table productsTable = dataManager.parseTable();
        var products = dataManager.parseProducts(productsTable.values());
        products.forEach(product -> {
            var article = product.getArticle();
            if (article.contains("onjoy") || article.contains("rl-") || article.contains("RL-")) {
                var id = String.valueOf(product.getId());
                var supplier = TableConfig.SuppliersNamesConfig.SexTrgSupplierConst;
                productsTable.updateRowValue(id, SUPPLIER_COL_NAME, supplier);
            }
        });
        dataManager.writeAll(productsTable);
    }

}
