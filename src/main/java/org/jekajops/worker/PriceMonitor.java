package org.jekajops.worker;

import org.jekajops.app.loger.Loggable;
import org.jekajops.entities.OzonProduct;
import org.jekajops.entities.Product;
import org.jekajops.entities.Table;
import org.jekajops.integrate.ozon.OzonManager;
import org.jekajops.parser.exel.DataManager;
import org.jekajops.parser.exel.DataManagerFactory;
import org.jekajops.parser.shop.OzonParserSe;
import org.jekajops.parser.shop.ShopParser;
import org.jekajops.parser.util.XmarketParser;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.stream.Collectors;

import static org.jekajops.app.cnfg.AppConfig.logger;
import static org.jekajops.app.cnfg.TableConfig.WebDocConfig.*;
import static org.jekajops.app.cnfg.TableConfig.OzonConfig.*;

public class PriceMonitor implements Runnable, Loggable {
    @Override
    public void run() {
        runTask();
    }

    public void runTask() {
        ShopParser shopParser = null;
        try {
            DataManager dataManager = DataManagerFactory.getOzonWebCsvManager();
            Table table = dataManager.parseTable();
            try {
                table = getUpdatedOzonTable(table);
            } catch (Throwable t) {
               t.printStackTrace();
            }
            var productsList = dataManager.parseProducts(table.values());
            var productsQueue = new ArrayBlockingQueue<Product>(productsList.size());
            Collections.shuffle(productsList);
            productsQueue.addAll(productsList);
            shopParser = new OzonParserSe();
            while (!productsQueue.isEmpty()) {
                var product = productsQueue.poll();
                var searchKey = product.getBarcode();
                var searchBarcode = ((OzonProduct) product).getSearchBarcode();
                if (searchKey.contains("OZN")) {
                    if (searchBarcode != null && !searchBarcode.isEmpty()) {
                        searchKey = searchBarcode;
                    } else {
                        searchKey = XmarketParser.parseBarcode(product.getArticle());
                        if (searchKey != null) searchBarcode = searchKey;
                    }
                }
                if (searchKey == null) continue;
                Thread.sleep(1000 + new Random().nextInt(3 * 1000));
                var actualPrice = product.getPrice();
                log("product: " + product.toString());
                var parsedProducts = shopParser.parseProducts(searchKey);
                if (parsedProducts.isEmpty()) {
                    log("no products was found");
                    continue;
                }
                var lowerPriceProduct = shopParser.getLowerPriceProduct(parsedProducts, product);
                var lowerPrice = lowerPriceProduct.getPrice();
                if (lowerPrice < actualPrice) {
                    log("actualPrice = " + actualPrice);
                    log("lowerPrice = " + lowerPrice);
                }
                var mapKey = String.valueOf(product.getId());
                var map = table.get(mapKey);
                var diff = lowerPrice - actualPrice;
                var href = ((OzonProduct) lowerPriceProduct).getConcurrentProductUrl();
                if (href == null || href.isEmpty()) href = map.get(CONCURRENT_URL_COL_NAME);
                map.put(LOWER_PRICE_COL_NAME, String.valueOf(lowerPrice));
                map.put(DIFF_PRICES_COL_NAME, String.valueOf(diff));
                map.put(CONCURRENT_URL_COL_NAME, href);
                map.put(SEARCH_BARCODE_COL_NAME, searchBarcode);
                table.put(mapKey, map);
                log("Updated!");
                dataManager.writeAll(table);
            }
        } catch (Throwable e) {
            e.printStackTrace();
            log(String.valueOf(e));
        } finally {
            if (shopParser != null) {
                shopParser.quit();
            }
        }
    }

    public static Table getUpdatedOzonTable(Table table) throws Throwable {
        var ozonManager = new OzonManager();
        var actualPricesProductsMap = ozonManager.getActualPricesProducts().stream().map(product -> (OzonProduct) product).collect(Collectors.toMap(OzonProduct::getOzonProductId, product -> product));
        var keys = actualPricesProductsMap.keySet();
        var mapsWithAnotherKey = new Table(OZON_PRODUCT_ID_COL_NAME, new ArrayList<>(table.getKeys()), new ArrayList<>(table.values()));
        keys.forEach(key -> {
            var currentPriceMap = mapsWithAnotherKey.get(key);
            if (currentPriceMap != null) {
                var updatedPriceProduct = actualPricesProductsMap.get(key);
                var updatedPrice = String.valueOf(updatedPriceProduct.getPrice());
                if (!currentPriceMap.get(PRICE_COL_NAME).equals(updatedPrice)) {
                    logger.log(PriceMonitor.class.toString(), "was = " + currentPriceMap.get(PRICE_COL_NAME) + " & updatedPrice = " + updatedPrice);
                }
                currentPriceMap.put(PRICE_COL_NAME, updatedPrice);
                mapsWithAnotherKey.put(key, currentPriceMap);
            }

        });
        return new Table(ID_COL_NAME, new ArrayList<>(table.getKeys()), new ArrayList<>(mapsWithAnotherKey.values()));
    }

}
