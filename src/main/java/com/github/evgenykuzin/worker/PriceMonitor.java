package com.github.evgenykuzin.worker;

import com.github.evgenykuzin.core.data_managers.DataManagerFactory;
import com.github.evgenykuzin.core.data_managers.WebCsvDataManager;
import com.github.evgenykuzin.core.db.OzonProductDAO;
import com.github.evgenykuzin.core.entities.OzonProduct;
import com.github.evgenykuzin.core.entities.Product;
import com.github.evgenykuzin.core.entities.Table;
import com.github.evgenykuzin.core.util.loger.Loggable;
import com.github.evgenykuzin.parser.OzonParserSe;
import com.github.evgenykuzin.parser.ShopParser;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;

import static com.github.evgenykuzin.core.cnfg.TableConfig.AdditionalOzonDocFieldsConfig.*;

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
            var productsList = dataManager.parseProducts();
            //OzonProductDAO ozonProductDAO = new OzonProductDAO();
            //List<OzonProduct> productsList = ozonProductDAO.getAll();
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
                //var ozonProductToUpdate = ozonProductDAO.get(ozonProduct.getId());
                var map = table.get(mapKey);
                var diff = lowerPrice - actualPrice;
                var href = ((OzonProduct) lowerPriceProduct).getConcurrentProductUrl();
                if (href == null || href.isEmpty()) href = map.get(CONCURRENT_URL_COL_NAME);
                //ozonProductToUpdate.setConcurrentPrice(lowerPrice);
                //ozonProductToUpdate.setConcurrentProductUrl(href);
                map.put(LOWER_PRICE_COL_NAME, String.valueOf(lowerPrice));
                map.put(DIFF_PRICES_COL_NAME, String.valueOf(diff));
                map.put(CONCURRENT_URL_COL_NAME, href);
                //ozonProductDAO.update(ozonProductToUpdate);
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

}
