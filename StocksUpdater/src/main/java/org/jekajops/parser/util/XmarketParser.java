package org.jekajops.parser.util;

import org.jekajops.entities.Product;
import org.jekajops.entities.Table;
import org.jekajops.parser.exel.CsvDataManager;
import org.jekajops.parser.exel.DataManagerFactory;
import org.jekajops.parser.exel.WebCsvDataManager;
import org.jekajops.util.FileManager;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.jekajops.app.cnfg.TableConfig.XmarketConfig.STOCKS_COL_NAME;
import static org.jekajops.app.cnfg.TableConfig.WebDocConfig.SEARCH_BARCODE_COL_NAME;
import static org.jekajops.app.cnfg.TableConfig.XmarketConfig.BARCODE_COL_NAME;

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

    public static List<Product> parseNewStocksProducts(List<Product> products) {
        var tradeTable = getTradeTable();
        var resultProducts = new ArrayList<Product>();
        products.forEach(product -> {
            var row = tradeTable.get(product.getArticle());
            if (row != null) {
                var stock = row.get(STOCKS_COL_NAME);
                if (stock != null && !stock.isEmpty()) {
                    product.setStock(Integer.parseInt(stock));
                    resultProducts.add(product);
                }
            }

        });
        return resultProducts;
    }

    public static void updateBarcodes(WebCsvDataManager dataManager) {
        Table tradeTable = getTradeTable();
        Table productsTable = dataManager.parseTable();
        var products = dataManager.parseProducts(productsTable.values());
        products.forEach(product -> {
            var article = product.getArticle();
            var tradeRaw = tradeTable.get(article);
            if (tradeRaw != null) {
                var barcode = tradeRaw.get(BARCODE_COL_NAME);
                var id = String.valueOf(product.getId());
                productsTable.updateRawValue(id, SEARCH_BARCODE_COL_NAME, barcode);
            }
        });
        dataManager.writeAll(productsTable);
    }

    private static Table getTradeTable() {
        File file = null;
        try {
            file = FileManager.download(
                    "https://www.xmarket.ru/upload/clients/trade.csv",
                    FileManager.getFromResources("trade.csv")
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
        CsvDataManager csvDataManager = DataManagerFactory.getXmarketCsvManager(file);
        return csvDataManager.parseTable();
    }

    private static Document getDocument(String url) throws IOException {
        return Jsoup.connect(url)
                .method(Connection.Method.GET)
                .followRedirects(false)
                .timeout(35000)
                .userAgent("Chrome")
                .get();
    }

    private static String getUrl(String url, Map<String, String> params) {
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

    public static void main(String[] args) {
        updateBarcodes(DataManagerFactory.getOzonWebCsvManager());
    }

}
