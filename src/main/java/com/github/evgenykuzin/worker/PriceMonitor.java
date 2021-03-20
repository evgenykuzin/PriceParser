package com.github.evgenykuzin.worker;

import com.github.evgenykuzin.core.api_integrations.ozon.OzonManager;
import com.github.evgenykuzin.core.data_managers.DataManagerFactory;
import com.github.evgenykuzin.core.data_managers.WebCsvDataManager;
import com.github.evgenykuzin.core.entities.OzonProduct;
import com.github.evgenykuzin.core.entities.Product;
import com.github.evgenykuzin.core.entities.Table;
import com.github.evgenykuzin.core.util.cnfg.LogConfig;
import com.github.evgenykuzin.core.util.loger.Loggable;
import com.github.evgenykuzin.parser.OzonParserSe;
import com.github.evgenykuzin.parser.ShopParser;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.stream.Collectors;

import static com.github.evgenykuzin.core.util.cnfg.TableConfig.AdditionalOzonDocFieldsConfig.*;
import static com.github.evgenykuzin.core.util.cnfg.TableConfig.OzonDocConfig.OZON_PRODUCT_ID_COL_NAME;
import static com.github.evgenykuzin.core.util.cnfg.TableConfig.OzonUpdateConfig.PRICE_COL_NAME;

public class PriceMonitor implements Runnable, Loggable {
    @Override
    public void run() {
        runTask();
    }

    public void runTask() {
        ShopParser shopParser = null;
        try {
            WebCsvDataManager dataManager = DataManagerFactory.getOzonWebCsvManager();
            Table table = dataManager.parseTable();
            try {
                table = getUpdatedOzonTable(table);
                dataManager.writeAll(table);
            } catch (Throwable t) {
               t.printStackTrace();
            }
            var productsList = dataManager.parseProducts(table.values());
            var productsQueue = new ArrayBlockingQueue<Product>(productsList.size());
            Collections.shuffle(productsList);
            productsQueue.addAll(productsList);
            shopParser = new OzonParserSe();
            while (!productsQueue.isEmpty()) {
                var ozonProduct = (OzonProduct) productsQueue.poll();
                var searchKey = ozonProduct.getBarcode();
                var searchBarcode = ozonProduct.getSearchBarcode();
                if (searchKey.contains("OZN")) {
                    if (searchBarcode != null && !searchBarcode.isEmpty()) {
                        searchKey = searchBarcode;
                    } else {
                        continue;
                    }
                }
                var actualPrice = ozonProduct.getPrice();
                log("ozonProduct: " + ozonProduct.toString());
                var parsedProducts = shopParser.parseProducts(searchKey);
                if (parsedProducts.isEmpty()) {
                    log("no products was found");
                    continue;
                }
                var lowerPriceProduct = shopParser.getLowerPriceProduct(parsedProducts, ozonProduct);
                var lowerPrice = lowerPriceProduct.getPrice();
                if (lowerPrice < actualPrice) {
                    log("actualPrice = " + actualPrice);
                    log("lowerPrice = " + lowerPrice);
                }
                var mapKey = String.valueOf(ozonProduct.getId());
                var map = table.get(mapKey);
                var diff = lowerPrice - actualPrice;
                var href = ((OzonProduct) lowerPriceProduct).getConcurrentProductUrl();
                if (href == null || href.isEmpty()) href = map.get(CONCURRENT_URL_COL_NAME);
                map.put(LOWER_PRICE_COL_NAME, String.valueOf(lowerPrice));
                map.put(DIFF_PRICES_COL_NAME, String.valueOf(diff));
                map.put(CONCURRENT_URL_COL_NAME, href);
                map.put(SEARCH_BARCODE_COL_NAME, searchBarcode);
                table.put(mapKey, map);
                log("concurrent price has updated!");
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
                var currentPrice = currentPriceMap.get(PRICE_COL_NAME);
                if (currentPrice != null && !currentPrice.equals(updatedPrice)) {
                    LogConfig.logger.log(PriceMonitor.class.toString(), "price was: " + currentPrice + " && updatedPrice is: " + updatedPrice);
                }
                currentPriceMap.put(PRICE_COL_NAME, updatedPrice);
                mapsWithAnotherKey.put(key, currentPriceMap);
            }
        });
        return new Table(ID_COL_NAME, new ArrayList<>(table.getKeys()), new ArrayList<>(mapsWithAnotherKey.values()));
    }

}
