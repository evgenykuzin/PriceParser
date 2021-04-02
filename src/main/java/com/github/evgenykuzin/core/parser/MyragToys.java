package com.github.evgenykuzin.core.parser;

import com.github.evgenykuzin.core.cnfg.TableConfig;
import com.github.evgenykuzin.core.data_managers.XlsxDataManager;
import com.github.evgenykuzin.core.entities.Product;
import com.github.evgenykuzin.core.entities.SupplierProduct;
import com.github.evgenykuzin.core.util.loger.Loggable;
import com.github.evgenykuzin.core.util_managers.FileManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MyragToys implements SupplierManager, Loggable {
    private final File fileToParse;

    public MyragToys() {
        this.fileToParse = FileManager.getFromResources("MyragToys.xls");
    }

    @Override
    public String getName() {
        return "MyragToys";
    }

    @Override
    public List<Product> parseNewStocksProducts(List<Product> products) {
        List<Product> result = new ArrayList<>();
        var supplierProducts = parseProducts();
        products.forEach(product -> {
            Product supProduct = null;
            for (var p : supplierProducts) {
                if (product.getArticle() == null) break;
                if (p.getArticle() == null) continue;
                if (product.getArticle().equals(p.getArticle())) {
                    supProduct = p;
                    break;
                }
            }
            if (supProduct != null) {
                var stock = supProduct.getStock();
                if (stock != null) {
                    product.setStock(stock);
                    result.add(product);
                }
            } else {
                log("supProduct of " + product.getName() + " is incorrect");
            }
        });
        return result;
    }

    @Override
    public List<Product> parseProducts() {
        var dataManager = new XlsxDataManager(
                fileToParse,
                TableConfig.MiragToysConfig.ARTICLE_COL_NAME
        ) {
            @Override
            public void removeGarbageFromData(List<List<Object>> data) {
                //TODO
            }
        };
        return dataManager.parseProductsList(row -> {
                    var idStr = row.get(TableConfig.MiragToysConfig.ID_COL_NAME);
                    var id = idStr == null ? null : (long) Double.parseDouble(idStr);
                    var priceStr = row.get(TableConfig.MiragToysConfig.PRICE_COL_NAME);
                    var price = priceStr == null ? null : Double.parseDouble(priceStr);
                    var name = row.get(TableConfig.MiragToysConfig.NAME_COL_NAME);
                    var brand = row.get(TableConfig.MiragToysConfig.BRAND_COL_NAME);
                    String barcode = null;
                    var articleStr = row.get(TableConfig.MiragToysConfig.ARTICLE_COL_NAME);
                    String article = articleStr;
//                    try {
//                        article = articleStr == null ? null : String.valueOf((int) Double.parseDouble(articleStr));
//                    } catch (NumberFormatException ignore) {
//                        article = articleStr;
//                    }
                    var stocksStr = row.get(TableConfig.MiragToysConfig.STOCKS_COL_NAME);
                    var stocks = stocksStr == null ? null : (int) Double.parseDouble(stocksStr);
                    return new SupplierProduct(id, idStr, price, name, brand, barcode, article, stocks, getName());
                }
        );
    }

    @Override
    public boolean sendOrders(Collection<? extends Product> products) {
        return false;
    }

}
