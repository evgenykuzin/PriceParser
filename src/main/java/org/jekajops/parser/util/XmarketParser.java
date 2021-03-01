package org.jekajops.parser.util;

import org.jekajops.app.cnfg.AppConfig;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.HashMap;
import java.util.Map;

public class XmarketParser {

    private static final String URL = "https://www.xmarket.ru/";
    private static final String URL_CAT = "catalog/";


    public static String parseBarcode(String name) throws IOException {
        System.out.println("name for searching in xmarket = " + name);
        var params = new HashMap<String, String>();
        params.put("q", name);
        params.put("s", "");
        var sercher = getDocument(getUrl(URL + URL_CAT, params));
        var searchResult = sercher
                .getElementsByClass("product-item-image-wrapper");
        if (searchResult.isEmpty()) {
            System.out.println("searchResult in xmarket is empty");
            return null;
        }
        String href = searchResult.get(0).attr("href");
        String barcode = null;
        int i = 0;
        while (barcode == null) {
            try {
                String url = getUrl(URL + href, new HashMap<>());
                sercher = getDocument(url);
                barcode = sercher
                        .getElementsByClass("product-item-detail-tab-content").get(1)
                        .getElementsByTag("dl").get(0)
                        .getElementsByTag("dd").get(1)
                        .text();
            } catch (IndexOutOfBoundsException e) {
                i++;
                if (i == 5) {
                    System.out.println("Elements not found in xmarket");
                    return null;
                }
            }
        }
        return barcode;
    }

    private static Document getDocument(String url) throws IOException {
        Proxy proxy = new Proxy(
                Proxy.Type.HTTP,
                InetSocketAddress.createUnresolved("146.66.172.217", 8080)
        );
        return Jsoup.connect(url)
                //.proxy(proxy)
                .method(Connection.Method.GET)
                .followRedirects(false)
                .timeout(35000)
                .userAgent("Chrome")
                .get();
    }

    public static String parseBarcode2(String name) {
        String barcode = null;
        try (CloseableWebDriverWrapper closeableWebDriverWrapper = new CloseableWebDriverWrapper()) {
            WebDriver webDriver = closeableWebDriverWrapper.getWebDriver();
            System.out.println("name for searching = " + name);
            var params = new HashMap<String, String>();
            params.put("q", name);
            params.put("s", "");
            String url = getUrl(URL + URL_CAT, params);
            System.out.println("url = " + url);
            webDriver.get(url);
            var searchResult = webDriver
                    .findElements(By.className("product-item-image-wrapper"));
            if (searchResult.isEmpty()) {
                System.out.println("searchResult is empty");
                return null;
            }
            String href = searchResult.get(0).getAttribute("href");
            int i = 0;
            while (barcode == null) {
                webDriver.get(href);
                try {
                    barcode = webDriver
                            .findElements(By.className("product-item-detail-tab-content")).get(1)
                            .findElements(By.tagName("dl")).get(0)
                            .findElements(By.tagName("dd")).get(1)
                            .getText();
                    System.out.println("barcode = " + barcode);
                } catch (IndexOutOfBoundsException e) {
                    i++;
                    Thread.sleep(5000);
                    if (i == 10) break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return barcode;
    }

    static String getUrl(String url, Map<String, String> params) {
        if (params.isEmpty()) return url;
        StringBuilder urlBuilder = new StringBuilder(url);
        urlBuilder.append("?");
        params.forEach((key, value) -> urlBuilder
                .append(key)
                .append("=")
                .append(value)
                .append("&"));
        urlBuilder.deleteCharAt(urlBuilder.lastIndexOf("&"));
        return urlBuilder.toString();
    }

    private static class CloseableWebDriverWrapper implements AutoCloseable{
        private final WebDriver webDriver;

        public CloseableWebDriverWrapper() {
            webDriver = AppConfig.getWebDriver();
        }

        public WebDriver getWebDriver() {
            return webDriver;
        }

        @Override
        public void close() throws Exception {
            if (webDriver != null) webDriver.quit();
        }
    }

}
