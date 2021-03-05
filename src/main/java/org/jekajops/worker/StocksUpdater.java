package org.jekajops.worker;

import org.jekajops.entities.OzonProduct;
import org.jekajops.integrate.ozon.OzonManager;
import org.jekajops.parser.exel.DataManagerFactory;
import org.jekajops.parser.util.XmarketParser;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

public class StocksUpdater implements Runnable {
    @Override
    public void run() {
        var ozonManager = new OzonManager();
        try {
            var inf = ozonManager.executePostRequest("/v1/warehouse/list");
            System.out.println("res /v1/warehouse/list = " + inf);
        } catch (IOException e) {
            e.printStackTrace();
        }
        var dataManager = DataManagerFactory.getOzonWebCsvManager();
        var products = dataManager.parseProducts(dataManager.parseTable().values());
        var warehouseId = "20658102477000";
        AtomicInteger i = new AtomicInteger(1);
        products.forEach(product -> {
            if (i.get() == 1) {
                System.out.println("product = " + product);
                i.set(5);
                try {
                    ozonManager.updateProductStocks(
                            (OzonProduct) product,
                            warehouseId,
                            XmarketParser.parseStock(product.getArticle())
                    );
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static void main(String[] args) {
        new StocksUpdater().run();
    }
}
