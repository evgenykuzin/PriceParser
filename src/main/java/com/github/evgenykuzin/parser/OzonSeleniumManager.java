package com.github.evgenykuzin.parser;

import com.github.evgenykuzin.core.entities.OzonProduct;
import com.github.evgenykuzin.core.entities.Product;
import com.github.evgenykuzin.core.util.SearchMatcher;
import com.github.evgenykuzin.core.util.loger.Loggable;
import com.github.evgenykuzin.core.util_managers.AntiCaptchaManager;
import com.github.evgenykuzin.core.util_managers.FileManager;
import net.lightbody.bmp.BrowserMobProxy;
import net.lightbody.bmp.BrowserMobProxyServer;
import net.lightbody.bmp.client.ClientUtil;
import org.apache.commons.exec.environment.EnvironmentUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.firefox.internal.ProfilesIni;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;

import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated;

public class OzonSeleniumManager implements Loggable {
    private ChromeDriver webDriver;
    private final String url;
    private static final String anticaptchaKey = "53c05b90303e57b65fd6251cfe3c3081";

    public OzonSeleniumManager(String url) {
        this.url = url;
        initWebDriver();
        try {
            webDriver.get("https://www.ozon.ru/");
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
                if (brand != null) {
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
                        var split = href.split("\\?");
                        split = split[0].split("/");
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
                        products.add(new OzonProduct(0L, null, nameElement.getText(), null, barcode, barcode, Double.parseDouble(price), null, null, null, null, null, null, Double.parseDouble(price), href));
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
        var taskId = AntiCaptchaManager.createTask("6Ld38BkUAAAAAPATwit3FXvga1PI6iVTb6zgXw62", webDriver.getCurrentUrl());
        Thread.sleep(20000);
        return AntiCaptchaManager.getResult(taskId);
    }

    private String getSearchUrl(int page, String key) {
        System.out.println("page URL = " + String.format(url, page, key));
        return String.format(url, page, key);
    }

    private void waitingToSolveCaptcha() {
        new WebDriverWait(webDriver, 1000000).until(webDriver -> {
            if (webDriver.getPageSource().contains("ROBOTS")) {
                System.out.println(webDriver.getPageSource());
                try {
//                    var obj = ((ChromeDriver) webDriver).executeAsyncScript(jsScript2(anticaptchaKey));
//                    System.out.println("obj = " + obj);
//                    System.out.println(webDriver.getPageSource());
//                    Thread.sleep(120*1000);

//                    try {
//                        Thread.sleep(11000);
//                        var solution = "03AGdBq27UCUyHQok710IvmOv4GVOC9Qs7llcdOy-0XGilRixddUyOyqTv6QrjTZaor2ZGq4uEBkCQyln7Iphv7AJ9-UagC7El1Sftv_FV8aUTHpJ_Cu2eCngMBiAYCm-MaiMQMjnpTyeoohl3GK8vZcwoTxy0gLe0y-jW-rKzolmO_uC6SCI3wDMqqHfjmjy_z9De8eh5p6_W_k4xdZ7hlhVc33ctN8EqgWqveov0qDvDL09yaqp5hhOiXjWjciKvoAHmKNuMTKqe8dtXZ7jDkJwhmTG9wx8uEHjCIE-jviHzhBq84sQ-ieXdecAGTCkkOo3-Jx1VY1vRLjc4R7H0MFtaX9EYZFeJml_Ta6jvdz_4Tr7Zcq-A8kMOU9lEnh4o62Yrcf0wJve3_maCv5psJVcOYUddjvGxKyih4QlSeFDGK40OWjBuObfAAKHdwjsglZCs12gXKn_NxXo_k8dbGqqvqV9ooNKrs9Oe0oWQlFphF6XKIhHOrH4GhckMx7PjC2qIvB9HnBavOiaHmB-AfmR7gv0bWiyn65tPCnuQU7eOfMA7VqEB00-zo1QFWS3odlrvZMzOJ5wQEICFZoWhFryrVnQwuWPX3JNFGu7yQlIWqIX41xccuxNKfR2t2qR517XoJrH0BdXMeFTxYSCJFV2h3506j1A0J5upfeptZLU1ew4LqHEHJGYH7SSrnb50qAf2WDrG5pcEIxqdK6e_KP7lb_dv_ukd6eihbNNPi_p-Lmv3V7ITJGv6C7ZUHKD_D2Hr_WeWETz7QzEiofyn83zwDaUe_SSR4m6kTpQRm03_jTpIuTLeqzOlCKQDeRHsU5bAkOn2A3-0MVoMteJWLzMUKrn4QS2TZXmgOgC8bQCBC-VZxXmdMPNWYZU7OpbNeMmJeCfe_iLA7iAvjJRWE1d3DmZafkhYJ-Qf9JJZ56cDor1Jn3KOVxEg804kSkTnHFZgaSldjw-1qdkJA2i5ACMGq_3kQyjzOsIGY83R5ds9gdfjZIFVhyF2HTMNJiXoNwMxq7n6Qj50H3Vru690KCA5XSGY0-1mMoGkioMeIxNh91wxcvxRjbJEKZdFsHsjw-zzkt35TAkqFGO-CyfD_Ii6qs9DSPEI5OFAqUvy6n5xgtuGuGx9ZIMVKwQWn2iZxy-L0du_G9FPdLeH79Fe0HYPzaMrSo2GZ5QWe0Re2K6Vw30kmVciSf2_l0sRPGWL8zHf41QP1hOPmvcZk1FmFvMjHdVVsOKjYSBQ3LRlE9bWVsc8NerO7L03Ki-NbwbbxH1hNAQXGvXn00rEbuFOeACbHtYtNK-SIEoRg5MybAUG72gdV5U7u_1619XT4yqjP2u8vDHBUy84mBmssqZQeovpvQ9glqiL_f--B3Ox5SuQ-kbZOrtL6MhB_M-lZswMmkw3XYGAhiUjT4j6ITENm6vWijCV8SifTI5tZGTl1_C0zTuACR5mHZQI4dpSMyTYimxXbrnCssyE";
//                        ((FirefoxDriver) webDriver).executeScript("(function() { area = document.getElementById(\""+"g-recaptcha-response"+"\"); " + "area.value = \""+ solution + "\"; } )();");
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
                } catch (Throwable t) {
                    t.printStackTrace();
                }
                return false;
            }

            return true;
        });
    }

    public void initWebDriver() {
        webDriver = getChromeWebDriver();
        if (webDriver == null) throw new NullPointerException("WebDriver == null");
    }

    public FirefoxDriver getFirefoxDriver() {
        try {
            System.setProperty("GOOGLE_CHROME_BIN", "/app/.apt/usr/bin/google-chrome");
            System.setProperty("CHROMEDRIVER_PATH", "/app/.chromedriver/bin/chromedriver");
            FirefoxOptions options = new FirefoxOptions();
            if (getOS().contains("win")) {
                System.setProperty("webdriver.gecko.driver", FileManager.getFromResources("geckodriver.exe").getAbsolutePath());
            } else {
                //System.setProperty("webdriver.chrome.driver", EnvironmentUtils.getProcEnvironment().get("CHROMEDRIVER_PATH"));
                String binaryPath = null;
                try {
                    binaryPath = EnvironmentUtils.getProcEnvironment().get("GECKODRIVER_PATH");
                } catch (IOException e) {
                    e.printStackTrace();
                    log(e.getMessage());
                }
                log("Webdriver Binary Path: " + binaryPath);
                options.setBinary(binaryPath);
            }
            DesiredCapabilities capabilities = DesiredCapabilities.firefox();
            options.addArguments("--enable-javascript");
            //options.addArguments("--headless");
            options.addArguments("--disable-gpu");
            options.addArguments("--no-sandbox");
            //options.addArguments("--disable-extensions");
            //options.addArguments("--disable-dev-shm-usage");
            options.setAcceptInsecureCerts(true);
            options.setPageLoadStrategy(PageLoadStrategy.NORMAL);
            BrowserMobProxy proxy = setUpProxy();
            options.setProxy(ClientUtil.createSeleniumProxy(proxy));
            //FirefoxProfile firefoxProfile = new FirefoxProfile(new File("C:\\Users\\JekaJops\\AppData\\Local\\Mozilla\\Firefox\\Profiles\\nahd6ha2.default"));
            ProfilesIni profile = new ProfilesIni();
            FirefoxProfile firefoxProfile = profile.getProfile("default");
            firefoxProfile.addExtension(FileManager.getFromResources("anticaptcha-plugin_v0.52.xpi"));
            options.setProfile(firefoxProfile);
            options.merge(capabilities);
            var webDriver = new FirefoxDriver(options);
            webDriver.executeScript(getAuthorizeAnticaptchaScript(anticaptchaKey));
            return webDriver;
        } catch (Throwable t) {
            t.printStackTrace();
            log(t.getMessage());
        }
        return null;
    }

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
            options.addArguments(String.format("user-data-dir=%s", FileManager.getFromResources("User Data").getAbsolutePath()));
            options.setPageLoadStrategy(PageLoadStrategy.NORMAL);
            options.addExtensions(FileManager.getFromResources("anticaptcha-plugin_v0.52.crx"));
            BrowserMobProxy proxy = setUpProxy();
            options.setProxy(ClientUtil.createSeleniumProxy(proxy));
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

    private static String getAuthorizeAnticaptchaScript(String anticaptchaKey) {
        return String.format("return window.postMessage({\n" +
                        "        receiver: \"antiCaptchaPlugin\",\n" +
                        "        data: {'options': {'antiCaptchaApiKey': '%s'}},\n" +
                        "        type: \"setOptions\"\n" +
                        "    }, window.location.href);\n", anticaptchaKey);
    }


    private static String jsScript2(String anticaptchaKey) {
        return String.format("(function(){\n" +
                "    var d = document.getElementById(\"anticaptcha-imacros-account-key\");\n" +
                "    if (!d) {\n" +
                "        d = document.createElement(\"div\");\n" +
                "        d.innerHTML = \"%s\";\n" +
                "        d.style.display = \"none\";\n" +
                "        d.id = \"anticaptcha-imacros-account-key\";\n" +
                "        document.body.appendChild(d);\n" +
                "    }\n" +
                "\n" +
                "    var s = document.createElement(\"script\");\n" +
                "    s.src = \"https://cdn.antcpt.com/imacros_inclusion/recaptcha.js?\" + Math.random();\n" +
                "    document.body.appendChild(s);\n" +
                "})();", anticaptchaKey);
    }

    private static String jsScript(String anticaptchaKey) {
        return String.format("(function(){\n" +
                "    var d = document.getElementById(\"anticaptcha-imacros-account-key\");\n" +
                "    if (!d) {\n" +
                "        d = document.createElement(\"div\");\n" +
                "        d.innerHTML = \"%s\";\n" +
                "        d.style.display = \"none\";\n" +
                "        d.id = \"anticaptcha-imacros-account-key\";\n" +
                "        document.body.appendChild(d);\n" +
                "    }\n" +
                "\n" +
                "(function() {\n" +
                "\n" +
                "    // TODO:\n" +
                "    // РџРѕРјРµРЅСЏС‚СЊ anticaptcha-imacros-account-key РЅР° РєР°РєРѕР№-РЅРёР±СѓРґСЊ anticaptcha-account-key,\n" +
                "    // РґР°Р±С‹ РЅРµ РїСЂРёРІСЏР·С‹РІР°С‚СЊСЃСЏ Рє imacros РґР»СЏ, РЅР°РїСЂРёРјРµСЂ, headless Р±СЂР°СѓР·РµСЂРѕРІ.\n" +
                "    // РЎ РѕР±СЂР°С‚РЅРѕР№ СЃРѕРІРјРµСЃС‚РёРјРѕСЃС‚СЊСЋ, РєРѕРЅРµС‡РЅРѕ!\n" +
                "\n" +
                "    // location.origin polyfill for IE\n" +
                "    if (!window.location.origin) {\n" +
                "        window.location.origin = window.location.protocol + \"//\"\n" +
                "            + window.location.hostname\n" +
                "            + (window.location.port ? ':' + window.location.port : '');\n" +
                "    }\n" +
                "\n" +
                "    /*jquery inclusion>*/\n" +
                "    var s = document.createElement(\"script\");\n" +
                "    s.src = \"https://code.jquery.com/jquery-1.12.4.min.js\";\n" +
                "    document.body.appendChild(s);\n" +
                "    /*<jquery inclusion*/\n" +
                "\n" +
                "    /*anticaptcha inclusion>*/\n" +
                "    var s = document.createElement(\"script\");\n" +
                "    s.src = \"https://cdn.antcpt.com/imacros_inclusion/anticaptcha/anticaptcha.js?\" + Math.random();\n" +
                "    document.body.appendChild(s);\n" +
                "    /*<anticaptcha inclusion*/\n" +
                "\n" +
                "    // myJquery\n" +
                "    /*\n" +
                "    var $$ = function() {\n" +
                "        var myJquery = {};\n" +
                "        myJquery.getById = function(id) {\n" +
                "            return document.getElementById(id);\n" +
                "        }\n" +
                "        myJquery.getByClassName = function(id) {\n" +
                "            var elements = document.getElementsByClassName();\n" +
                "            // todo: do more\n" +
                "        }\n" +
                "        return myJquery;\n" +
                "    }\n" +
                "    */\n" +
                "\n" +
                "    function getIframeSiteKey(iframeUrl) {\n" +
                "        return iframeUrl.replace(/.*k=([^&]+)&.*/, '$1');\n" +
                "    }\n" +
                "\n" +
                "    function callUserCallbackMethodIfExists(captchaSolvingInfo) {\n" +
                "        if (typeof anticaptchaUserCallbackMethod === 'function') {\n" +
                "            anticaptchaUserCallbackMethod.apply(null, arguments);\n" +
                "        }\n" +
                "    }\n" +
                "\n" +
                "    var $antigateSolver;\n" +
                "    var $gRecaptchaResponse;\n" +
                "    var $gRecaptchaFrameContainer;\n" +
                "\n" +
                "    var taskInProcessOfSolution = false;\n" +
                "\n" +
                "    var recaptchaCallbackAlreadyFired = false;\n" +
                "\n" +
                "    var checkModulesLoadedInterval = setInterval(function() {\n" +
                "\n" +
                "        // console.log('checkModulesLoadedInterval');\n" +
                "\n" +
                "        if (typeof Anticaptcha != 'undefined' && typeof jQuery != 'undefined') {\n" +
                "            clearInterval(checkModulesLoadedInterval);\n" +
                "            $.noConflict();\n" +
                "            // console.log(Anticaptcha);\n" +
                "\n" +
                "            $antigateSolver = jQuery();\n" +
                "            $gRecaptchaResponse =  jQuery();\n" +
                "            $gRecaptchaFrameContainer = jQuery();\n" +
                "\n" +
                "            // new method of getting Anti-Captcha API key\n" +
                "            var ACCOUNT_KEY_HERE = jQuery('#anticaptcha-imacros-account-key').html();\n" +
                "\n" +
                "            // old one\n" +
                "            if (!ACCOUNT_KEY_HERE) {\n" +
                "                ACCOUNT_KEY_HERE = jQuery('.g-recaptcha-response').val();\n" +
                "            }\n" +
                "\n" +
                "            // console.log('ACCOUNT_KEY_HERE=', ACCOUNT_KEY_HERE);\n" +
                "\n" +
                "            // Go, baby!\n" +
                "            // searching for recaptcha every second\n" +
                "            setInterval(function () {\n" +
                "                //document.body.style.backgroundColor = 'orange';\n" +
                "\n" +
                "                // console.log('g-recaptcha-response check');\n" +
                "\n" +
                "                jQuery('.g-recaptcha-response:not([anticaptured])').each(function () {\n" +
                "                    var $gRecaptchaResponseLocal = jQuery(this); // textarea.g-recaptcha-response\n" +
                "\n" +
                "                    $gRecaptchaResponseLocal.show();\n" +
                "\n" +
                "                    // find iframe\n" +
                "                    var $recaptchaIframe = $gRecaptchaResponseLocal.parent().find('iframe');\n" +
                "                    if (!$recaptchaIframe.length || !$recaptchaIframe.attr('src')) {\n" +
                "                        return;\n" +
                "                    }\n" +
                "\n" +
                "                    // get siteKey\n" +
                "                    var iframeUrl = parseUrl($recaptchaIframe.attr('src'));\n" +
                "                    var siteKey = getIframeSiteKey(iframeUrl.search); //iframeUrl.search.replace(/.*k=([^&]+)&.*/, '$1');\n" +
                "                    if (!siteKey || iframeUrl.search == siteKey) { // Couldn't get parameter K from url\n" +
                "                        return;\n" +
                "                    }\n" +
                "\n" +
                "                    // decode and trim possible spaces\n" +
                "                    siteKey = jQuery.trim(decodeURIComponent(siteKey));\n" +
                "\n" +
                "                    var stoken = null;\n" +
                "                    if (iframeUrl.search.indexOf('stoken=') != -1) {\n" +
                "                        stoken = iframeUrl.search.replace(/.*stoken=([^&]+)&?.*/, '$1');\n" +
                "                    }\n" +
                "\n" +
                "                    var $gRecaptchaRepresentativeLocal = $gRecaptchaResponseLocal.parent().parent();\n" +
                "                    var recaptchaDataSValue = $gRecaptchaRepresentativeLocal.attr('data-s');\n" +
                "\n" +
                "                    // do not try to handle this textarea anymore\n" +
                "                    $gRecaptchaResponseLocal.attr('anticaptured', 'anticaptured');\n" +
                "\n" +
                "                    var $gRecaptchaFrameContainerLocal = $gRecaptchaResponseLocal.prev('div');\n" +
                "                    var $gRecaptchaContainerLocal = $gRecaptchaResponseLocal.parent();\n" +
                "                    $gRecaptchaContainerLocal.height('auto');\n" +
                "\n" +
                "                    $gRecaptchaContainerLocal.append('<div class=\"antigate_solver\">AntiCaptcha</div>');\n" +
                "                    var $antigateSolverOne = $gRecaptchaContainerLocal.find('.antigate_solver');\n" +
                "\n" +
                "                    // Globals\n" +
                "                    // solver messages\n" +
                "                    $antigateSolver = $antigateSolver.add($antigateSolverOne);\n" +
                "                    // solution textarea\n" +
                "                    $gRecaptchaResponse = $gRecaptchaResponse.add($gRecaptchaResponseLocal);\n" +
                "                    // paint blue flag\n" +
                "                    $gRecaptchaFrameContainer = $gRecaptchaFrameContainer.add($gRecaptchaFrameContainerLocal);\n" +
                "\n" +
                "                    if (taskInProcessOfSolution) {\n" +
                "                        return;\n" +
                "                    }\n" +
                "\n" +
                "                    taskInProcessOfSolution = true;\n" +
                "\n" +
                "                    var anticaptcha = Anticaptcha(ACCOUNT_KEY_HERE);\n" +
                "\n" +
                "                    // recaptcha key from target website\n" +
                "                    anticaptcha.setWebsiteURL(window.location.origin);\n" +
                "                    anticaptcha.setWebsiteKey(siteKey); // 12345678901234567890123456789012\n" +
                "                    if (stoken) {\n" +
                "                        anticaptcha.setWebsiteSToken(stoken);\n" +
                "                    }\n" +
                "                    if (recaptchaDataSValue) {\n" +
                "                        anticaptcha.setRecaptchaDataSValue(recaptchaDataSValue);\n" +
                "                    }\n" +
                "                    anticaptcha.setSoftId(802);\n" +
                "\n" +
                "                    $antigateSolver.removeClass('error');\n" +
                "\n" +
                "                    // browser header parameters\n" +
                "                    anticaptcha.setUserAgent(\"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/52.0.2743.116\");\n" +
                "\n" +
                "                    anticaptcha.createTaskProxyless(function (err, taskId) {\n" +
                "                        if (err) {\n" +
                "                            $antigateSolver.removeClass().addClass('antigate_solver').addClass('error').text(err.message);\n" +
                "                            console.error(err);\n" +
                "\n" +
                "                            callUserCallbackMethodIfExists({\n" +
                "                                status: 'error'\n" +
                "                            });\n" +
                "\n" +
                "                            return;\n" +
                "                        }\n" +
                "\n" +
                "                        $antigateSolver.text('Solving is in process...');\n" +
                "                        $antigateSolver.addClass('in_process');\n" +
                "\n" +
                "                        callUserCallbackMethodIfExists({\n" +
                "                            status: 'in_process'\n" +
                "                        });\n" +
                "\n" +
                "                        // console.log(taskId);\n" +
                "\n" +
                "                        anticaptcha.getTaskSolution(taskId, function (err, taskSolution) {\n" +
                "                            if (err) {\n" +
                "                                $antigateSolver.removeClass().addClass('antigate_solver').addClass('error').text(err.message);\n" +
                "                                console.error(err);\n" +
                "\n" +
                "                                callUserCallbackMethodIfExists({\n" +
                "                                    status: 'error'\n" +
                "                                });\n" +
                "\n" +
                "                                return;\n" +
                "                            }\n" +
                "\n" +
                "                            $antigateSolver.text('Solved');\n" +
                "                            $antigateSolver.removeClass().addClass('antigate_solver').addClass('solved');\n" +
                "\n" +
                "                            callUserCallbackMethodIfExists({\n" +
                "                                status: 'solved'\n" +
                "                            });\n" +
                "\n" +
                "                            $gRecaptchaFrameContainer.append('DONE!');\n" +
                "\n" +
                "                            // console.log(taskSolution);\n" +
                "                            $gRecaptchaResponse.val(taskSolution);\n" +
                "\n" +
                "                            // CALLBACK HERE\n" +
                "\n" +
                "                            if (typeof ___grecaptcha_cfg != 'undefined'\n" +
                "                                && typeof ___grecaptcha_cfg.clients != 'undefined') {\n" +
                "                                var oneVisibleRecaptchaClientKey = null;\n" +
                "\n" +
                "                                // I know, I go to hell after this\n" +
                "                                visible_recaptcha_element_search_loop:\n" +
                "                                    for (var i in ___grecaptcha_cfg.clients) {\n" +
                "                                        for (var j in ___grecaptcha_cfg.clients[i]) {\n" +
                "                                            // check if it's a DOM element within IFRAME\n" +
                "                                            if (___grecaptcha_cfg.clients[i][j]\n" +
                "                                                && typeof ___grecaptcha_cfg.clients[i][j].nodeName == 'string'\n" +
                "                                                && typeof ___grecaptcha_cfg.clients[i][j].innerHTML == 'string'\n" +
                "                                                && typeof ___grecaptcha_cfg.clients[i][j].innerHTML.indexOf('iframe') != -1) {\n" +
                "\n" +
                "                                                // console.log('That element');\n" +
                "                                                // console.log(___grecaptcha_cfg.clients[i][j]);\n" +
                "\n" +
                "                                                // $(___grecaptcha_cfg.clients[0].Fc).is(\":visible\")\n" +
                "\n" +
                "                                                // check element visibility\n" +
                "                                                // $(___grecaptcha_cfg.clients[i][j]).is(\":visible\")\n" +
                "                                                if (___grecaptcha_cfg.clients[i][j].offsetHeight != 0\n" +
                "                                                    || (___grecaptcha_cfg.clients[i][j].childNodes.length && ___grecaptcha_cfg.clients[i][j].childNodes[0].offsetHeight != 0)\n" +
                "                                                    || ___grecaptcha_cfg.clients[i][j].dataset.size == 'invisible') { // IS VISIBLE for user or IS INVISIBLE type of reCaptcha\n" +
                "                                                    if (oneVisibleRecaptchaClientKey === null) {\n" +
                "                                                        oneVisibleRecaptchaClientKey = i;\n" +
                "                                                        // only one in this level of search\n" +
                "                                                        break;\n" +
                "                                                    } else {\n" +
                "                                                        // console.log('One only one visible recaptcha, break stuff!');\n" +
                "                                                        oneVisibleRecaptchaClientKey = null;\n" +
                "                                                        break visible_recaptcha_element_search_loop;\n" +
                "                                                    }\n" +
                "                                                }\n" +
                "                                            }\n" +
                "                                        }\n" +
                "                                    }\n" +
                "\n" +
                "                                // console.log('oneVisibleRecaptchaClientKey=');\n" +
                "                                // console.log(oneVisibleRecaptchaClientKey);\n" +
                "\n" +
                "                                if (oneVisibleRecaptchaClientKey !== null) {\n" +
                "                                    recursiveCallbackSearch(___grecaptcha_cfg.clients[oneVisibleRecaptchaClientKey], taskSolution, 1, 2);\n" +
                "                                }\n" +
                "                            }\n" +
                "\n" +
                "                            taskInProcessOfSolution = false;\n" +
                "                        });\n" +
                "                    });\n" +
                "                });\n" +
                "            }, 1000);\n" +
                "        }\n" +
                "    }, 200);\n" +
                "\n" +
                "    function parseUrl(url)\n" +
                "    {\n" +
                "        var parser = document.createElement('a');\n" +
                "        parser.href = url;\n" +
                "\n" +
                "        return parser;\n" +
                "\n" +
                "        parser.protocol; // => \"http:\"\n" +
                "        parser.hostname; // => \"example.com\"\n" +
                "        parser.port;     // => \"3000\"\n" +
                "        parser.pathname; // => \"/pathname/\"\n" +
                "        parser.search;   // => \"?search=test\"\n" +
                "        parser.hash;     // => \"#hash\"\n" +
                "        parser.host;     // => \"example.com:3000\"\n" +
                "    }\n" +
                "\n" +
                "    var recursiveCallbackSearch = function(object, solution, currentDepth, maxDepth) {\n" +
                "        if (recaptchaCallbackAlreadyFired) {\n" +
                "            return;\n" +
                "        }\n" +
                "\n" +
                "        var passedProperties = 0;\n" +
                "\n" +
                "        for (var i in object) {\n" +
                "            // console.log('i=', i);\n" +
                "    //                                    try {\n" +
                "    //                                        if (!object.hasOwnProperty(i)) {\n" +
                "    //                                            continue;\n" +
                "    //                                        }\n" +
                "    //                                    } catch (e) {\n" +
                "    //                                    }\n" +
                "\n" +
                "            passedProperties++;\n" +
                "\n" +
                "            // do not go farther\n" +
                "            if (passedProperties > 15) {\n" +
                "                break;\n" +
                "            }\n" +
                "\n" +
                "            // prevent \"Failed to read the 'contentDocument' property\" error\n" +
                "            try {\n" +
                "                if (typeof object[i] == 'object' && currentDepth <= maxDepth) { // she said not too deep\n" +
                "                    // console.log('RECURSIVE call for ', i);\n" +
                "                    recursiveCallbackSearch(object[i], solution, currentDepth + 1, maxDepth);\n" +
                "                    if (recaptchaCallbackAlreadyFired) {\n" +
                "                        return;\n" +
                "                    }\n" +
                "                } else if (i == 'callback') {\n" +
                "                    if (typeof object[i] == 'function') {\n" +
                "                        // console.log('CALLBACK ' + i + ' function with param +' + solution);\n" +
                "                        recaptchaCallbackAlreadyFired = true;\n" +
                "                        object[i](solution);\n" +
                "                    } else if (typeof object[i] == 'string' && typeof window[object[i]] == 'function') {\n" +
                "                        // console.log('CALLBACK ' + object[i] + ' global function with param +' + solution);\n" +
                "                        recaptchaCallbackAlreadyFired = true;\n" +
                "                        window[object[i]](solution);\n" +
                "                    }\n" +
                "\n" +
                "                    // one callback in this object\n" +
                "                    return;\n" +
                "                }\n" +
                "            } catch (e) {\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                "\n" +
                "})();" +
                "\n" +
                "})();", anticaptchaKey);
    }
}
