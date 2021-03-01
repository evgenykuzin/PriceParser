package org.jekajops.worker;

import org.jekajops.app.cnfg.AppConfig;
import org.jekajops.entities.Product;
import org.jekajops.parser.exel.DataManager;
import org.jekajops.parser.exel.DataManagerFactory;
import org.jekajops.parser.shop.OzonParserSe;
import org.jekajops.parser.shop.ShopParser;
import org.jekajops.parser.util.XmarketParser;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;

import static org.jekajops.app.cnfg.AppConfig.logger;

public class Worker implements Runnable {
    private ShopParser shopParser;

    public Worker() {
    }

    @Override
    public void run() {
        runTask();
    }

    private void log(String msg){
        logger.log(this.getClass().getName(), msg);
    }

    public void runTask(){
        try {
            //ExecutorService executorService = Executors.newCachedThreadPool();
            DataManager dataManager = DataManagerFactory.getOzonCsvManager(AppConfig.getExelPath());
            dataManager = DataManagerFactory.getOzonWebCsvManager();
            ShopParser shopParser = new OzonParserSe();
            var maps = dataManager.parseMaps();
            var productsList = dataManager.parseProducts(maps.values());
            var productsQueue = new ArrayBlockingQueue<Product>(productsList.size());
            productsQueue.addAll(new HashSet<>(productsList));
            var colsSet = new LinkedHashSet<>(maps.values().iterator().next().keySet());
            var colNames = colsSet.toArray(String[]::new);
            System.out.println("colNames = " + Arrays.toString(colNames));
            while (!productsQueue.isEmpty()) {
                //executorService.execute(() -> {
                var product = productsQueue.poll();
                var searchKey = product.getBarcode();
                if (searchKey.contains("OZN")) {
                    searchKey = XmarketParser.parseBarcode(product.getArticle());
                }
                System.out.println("searchKey = " + searchKey);
                if (searchKey == null) continue;
                var actualPrice = product.getPrice();
                log("product: "+product.toString());
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
                var map = maps.get(mapKey);
                map.put(AppConfig.NEW_PRICE_COL_NAME, String.valueOf(lowerPriceProduct.getPrice()));
                var diff = lowerPriceProduct.getPrice() - actualPrice;
                map.put(AppConfig.DIFF_PRICES_COL_NAME, String.valueOf(diff));
                maps.put(mapKey, map);
                dataManager.writeAll(maps.values(), colNames);
                //});
                Thread.sleep(12345);
            }
        } catch (Throwable e) {
            e.printStackTrace();
            log(e.getMessage());
        } finally {
            shopParser.quit();
        }
    }
}
