package com.github.evgenykuzin.worker;

import com.github.evgenykuzin.core.db.dao.DAO;
import com.github.evgenykuzin.core.db.dao.PriceDAO;
import com.github.evgenykuzin.core.db.dao.ProductDAO;
import com.github.evgenykuzin.core.entities.Price;
import com.github.evgenykuzin.core.util_managers.data_managers.DataManagerFactory;
import com.github.evgenykuzin.core.util_managers.data_managers.GoogleDocDataManager;
import com.github.evgenykuzin.core.entities.product.Product;
import com.github.evgenykuzin.core.entities.Table;
import com.github.evgenykuzin.core.util.loger.Loggable;
import com.github.evgenykuzin.parser.OzonSeleniumManager;
import org.hibernate.exception.JDBCConnectionException;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;


public class PriceMonitor implements Runnable, Loggable {
    private static final String URL = "https://www.ozon.ru/search/?page=%d&text=%s&from_global=true";

    @Override
    public void run() {
        runTask();
    }

    public void runTask() {
        OzonSeleniumManager shopParser = null;
        try {
            ProductDAO productDAO = ProductDAO.getInstance();
            PriceDAO priceDAO = PriceDAO.getInstance();
            var productsList = productDAO.getAll();
            //OzonProductDAO ozonProductDAO = new OzonProductDAO();
            //List<OzonProduct> productsList = ozonProductDAO.getAll();
            var productsQueue = new ArrayBlockingQueue<Product>(productsList.size());
            Collections.shuffle(productsList);
            productsQueue.addAll(productsList);
            shopParser = new OzonSeleniumManager(URL);
            while (!productsQueue.isEmpty()) {
                var product = productsQueue.poll();
                var ozonProduct = product.getOzonProduct();
                String searchKey;
                if (product.getBrandName() == null) continue;
                searchKey = product.getArticle() + " " + product.getBrandName();
                var actualPrice = ozonProduct.getPrice();
                log("product: " + product.toString());
                Price price;
                try {
                    price = priceDAO.getBy(new DAO.SearchEntry("product_id", String.valueOf(product.getId())));
                } catch (JDBCConnectionException jdbcce) {
                    jdbcce.printStackTrace();
                    continue;
                }
                if (price == null) price = new Price();
                price.setProduct(product);
                price.setPrice(actualPrice);

                var parsedPrice = shopParser.parseLowerPriceBySearchKey(price, searchKey);
                if (parsedPrice == null) {
                    log("no prices was found");
                    continue;
                }
                var lowerPrice = parsedPrice.getPrice();
                price.setConcurrentPrice(lowerPrice);
                var diff = lowerPrice - actualPrice;
                price.setPricesDiff(diff);
                price.setConcurrentUrl(parsedPrice.getConcurrentUrl());
                if (lowerPrice < actualPrice) {
                    log("actualPrice = " + actualPrice);
                    log("lowerPrice = " + lowerPrice);
                }

                priceDAO.saveOrUpdate(price);

//                var mapKey = String.valueOf(product.getId());
//                var map = table.get(mapKey);
                //ozonProductToUpdate.setConcurrentPrice(lowerPrice);
                //ozonProductToUpdate.setConcurrentProductUrl(href);
//                map.put(LOWER_PRICE_COL_NAME, String.valueOf(lowerPrice));
//                map.put(DIFF_PRICES_COL_NAME, String.valueOf(diff));
//                map.put(CONCURRENT_URL_COL_NAME, href);
                //ozonProductDAO.update(ozonProductToUpdate);
                //table.put(mapKey, map);
                log("concurrent price has updated!");
                //dataManager.writeAll(table);
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
