package com.github.evgenykuzin.parser;

import com.github.evgenykuzin.core.data_managers.DataManager;
import com.github.evgenykuzin.core.data_managers.DataManagerFactory;
import com.github.evgenykuzin.core.entities.Product;
import com.github.evgenykuzin.core.util.loger.Loggable;
import com.github.evgenykuzin.core.util.managers.FileManager;

import java.util.ArrayList;
import java.util.List;

public class MyragToysParser implements SupplierParser, Loggable {

    @Override
    public List<Product> parseNewStocksProducts(List<Product> products) {
        List<Product> result = new ArrayList<>();
        DataManager dataManager = DataManagerFactory
                .getMyragToysDataManager(FileManager
                        .getFromResources("Выгрузка_FULL_17.03.21.xls"));
        var supplierProducts = dataManager.parseProducts(dataManager.parseTable().values());
        products.forEach(product -> {
            var supProduct = supplierProducts.stream()
                    .filter(p -> product.getArticle() != null
                            && p.getArticle() != null
                            && p.getArticle().equals(product.getArticle()))
                    .findFirst()
                    .orElse(null);
            if (supProduct != null) {
                var stock = supProduct.getStock();
                if (stock != null) {
                    stock = stock > 0 ? stock - 1 : 0;
                    product.setStock(stock);
                    result.add(product);
                }
            } else {
                log("supProduct of "+product+" is incorrect");
            }
        });
        return result;
    }

}
