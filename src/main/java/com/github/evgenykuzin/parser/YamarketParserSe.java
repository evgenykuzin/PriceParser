package com.github.evgenykuzin.parser;

import com.github.evgenykuzin.core.cnfg.LogConfig;
import com.github.evgenykuzin.core.db.dao.ProductDAO;
import com.github.evgenykuzin.core.entities.product.Product;
import com.github.evgenykuzin.core.entities.product.YamarketProduct;
import com.github.evgenykuzin.core.util.loger.Loggable;
import com.github.evgenykuzin.core.util_managers.FileManager;
import com.github.evgenykuzin.core.db.dao.YamarketProductDAO;
import com.github.evgenykuzin.selenium.WebDriverConfig;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

import static org.openqa.selenium.support.ui.ExpectedConditions.*;

public class YamarketParserSe implements Loggable, Runnable {
    private static final WebDriverConfig webdriverConfig = new WebDriverConfig() {
    };
    private ChromeDriver webDriver;
    private final YamarketProductDAO dao;
    private static final String URL = "https://partner.market.yandex.ru/supplier/22032433/catalog?filterStatus=NEED_CONTENT%2CNEED_INFO";
    private static final File blackhrefsFile = FileManager.getFromResources("blackhrefs.txt");
    private static final File hrefsFile = FileManager.getFromResources("hrefs.txt");

    public YamarketParserSe() {
        dao = YamarketProductDAO.getInstance();
        this.webDriver = webdriverConfig.getChromeWebDriver();
    }

    public void initYamarket() {
        try {
            var wait = new WebDriverWait(webDriver, 20);
            webDriver.get(URL);
            if (webDriver.getCurrentUrl().equals(URL)) return;
            var loginInput = By.xpath(".//input[@id=\"passp-field-login\"]");
            var passInput = By.xpath(".//input[@id=\"passp-field-passwd\"]");
            var button = By.xpath(".//button[@type=\"submit\"]");
            wait.until(elementToBeClickable(loginInput));
            webDriver.findElement(loginInput).sendKeys("sellermp@yandex.ru");
            Thread.sleep(3000);
            webDriver.findElement(button).click();
            wait.until(elementToBeClickable(passInput));
            webDriver.findElement(passInput).sendKeys("rebpY0-pitdik-wudmuq");
            Thread.sleep(3000);
            webDriver.findElement(button).click();
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }
    }

    public void changeProducts() {
        var hrefs = getHrefs();
        Collections.shuffle(hrefs);
        for (var href : hrefs) {
            if (getBlackHrefs().contains(href)) continue;
            try {
                System.out.println("href = " + href);
                changeProduct(href);
                Thread.sleep(10000);
            } catch (Throwable t) {
                t.printStackTrace();
                webDriver.close();
                webDriver = webdriverConfig.getChromeWebDriver();
            }
        }
    }

    public void changeProduct(String href) {
        var wait = new WebDriverWait(webDriver, 1000 * 60);
        webDriver.get(href);
        var createCardLocator = By.xpath("//*[@id=\"app\"]/div/div[1]/div/div/div/div/div[5]/div[2]/div[1]/div/p[2]/a");
        //wait.until(visibilityOfElementLocated(createCardLocator));
        var createCard = webDriver.findElements(createCardLocator);

        if (createCard.isEmpty()) {
            saveBlackHref(href);
            return;
        }

        var hrefToCreate = createCard.get(0).getAttribute("href");
        webDriver.get(hrefToCreate);

        var formLocator = By.tagName("form");

        wait.until(visibilityOfElementLocated(formLocator));

        var form = webDriver.findElements(formLocator).get(0);

        if (webDriver.getPageSource().contains("GTIN")) {
            log("GTIN detected");
            var inputs = form.findElements(By.tagName("input"));
            WebElement inputBarcode;
            String inputText;
            inputBarcode = inputs.get(14);
            inputText = inputBarcode.getAttribute("value");
            System.out.println("inputText = " + inputText);
            if (inputText.isEmpty()) {
                inputBarcode = inputs.get(15);
                inputText = inputBarcode.getAttribute("value");
                System.out.println("inputText = " + inputText);
                if (inputText.isEmpty()) inputBarcode = null;
            }
            if (inputBarcode != null) {
                log("input for barcode founded");
                inputBarcode.sendKeys("\b".repeat(100));
            } else {
                log("input for barcode was not founded but GTIN problem was detected");
            }
        }
        var sku = form.findElements(By.tagName("input")).get(1).getAttribute("value");
        log("sku = " + sku);
        var nameText = form.findElements(By.tagName("input")).get(5).getAttribute("value");
        log("nameText = " + nameText);
        form.findElements(By.tagName("textarea")).get(0).sendKeys(nameText);
        List<String> imgs = getImages(sku);
        log("imgs = " + imgs);
        var imgInput = webDriver.findElements(By.xpath(".//*[@id=\"app\"]/div/div[1]/div/div/div/div/div/div/form/div[1]/div[2]/div[3]/div/span[1]/input")).get(0);
        for (String img : imgs) {
            imgInput.sendKeys(img);
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        webDriver.findElements(By.xpath(".//*[@id=\"app\"]/div/div[1]/div/div/div/div/div/div/form/div[2]/div/button")).get(0).submit();
        log("submit");
        saveBlackHref(href);

    }

    private List<String> getImages(String sku) {
        YamarketProduct yamarketProduct = dao.get(sku);
        Product product = ProductDAO.getInstance().get(yamarketProduct.getProductId());
        return product.getUrls()
                .stream()
                .map(url -> {
                    String path = null;
                    try {
                        var file = FileManager.download(url.replaceAll("\\\\", ""), "temp-for-yandex", ".jpg");
                        file.deleteOnExit();
                        path = file.getAbsolutePath();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return path;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private void saveBlackHref(String href) {
        try (FileWriter fw = new FileWriter(blackhrefsFile, true)) {
            fw.write(href + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<String> getBlackHrefs() {
        try {
            return Files.readAllLines(blackhrefsFile.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    private void saveAllHrefs(List<String> hrefs) {
        try (FileWriter fw = new FileWriter(hrefsFile, true)) {
            for (var href : hrefs) {
                fw.write(href + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<String> getHrefs() {
        try {
            return Files.readAllLines(hrefsFile.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    private void openAllPagination() {
        By moreLocator = By.xpath(".//button[@data-e2e=\"show-more-button\"]");
        new WebDriverWait(webDriver, 100000000).until(wd -> {
            var moreElements = wd.findElements(moreLocator);
            if (moreElements.isEmpty()) {
                return true;
            } else {
                moreElements.get(0).click();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return false;
            }
        });
    }

    private void parseHrefs() {
        initYamarket();
        openAllPagination();
        var tbody = webDriver.findElement(By.tagName("tbody"));
        var trows = tbody.findElements(By.xpath("tr"));

        LinkedList<String> hrefs = new LinkedList<>();

        for (var trow : trows) {
            try {
                var td = trow.findElements(By.tagName("td")).get(1);
                var href = td.findElements(By.tagName("a")).get(0).getAttribute("href");
                logf("href = %s", href);
                hrefs.add(href);
            } catch (IndexOutOfBoundsException iobe) {
                iobe.printStackTrace();
                if (!hrefs.isEmpty()) {
                    saveBlackHref(hrefs.getLast());
                }
                webDriver.get(URL);
            } catch (Exception e) {
                e.printStackTrace();
                webDriver.get(URL);
            }
        }

        saveAllHrefs(hrefs);
    }

    public void close() {
        webDriver.close();
    }

    public static void main(String[] args) {
        var x = new YamarketParserSe();
        x.run();
        //x.parseHrefs();
    }

    @Override
    public void run() {
        initYamarket();
        LogConfig.logger.log("init yamarket");
        int errorCount = 0;
        while (errorCount < 1000) {
            try {
                changeProducts();
                LogConfig.logger.log("founded");
                Thread.sleep(10000);
            } catch (Throwable t) {
                t.printStackTrace();
                errorCount++;
            }
        }
        close();
    }
}
