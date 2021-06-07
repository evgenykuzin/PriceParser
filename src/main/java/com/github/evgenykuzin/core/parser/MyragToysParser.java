package com.github.evgenykuzin.core.parser;

import com.github.evgenykuzin.core.cnfg.TableConfig;
import com.github.evgenykuzin.core.entities.product.Product;
import com.github.evgenykuzin.core.entities.product.SupplierProduct;
import com.github.evgenykuzin.core.util.loger.Loggable;
import com.github.evgenykuzin.core.util_managers.FTPManager;
import com.github.evgenykuzin.core.util_managers.data_managers.XlsxDataManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MyragToysParser implements SupplierParser, Loggable {

    public MyragToysParser() {
    }

    @Override
    public SUPPLIER_NAME getName() {
        return SUPPLIER_NAME.MyragToys;
    }

    @Override
    public List<SupplierProduct> parseNewStocksProducts(List<Product> products) {
        List<SupplierProduct> result = new ArrayList<>();
        var supplierProducts = parseProducts();
        products.forEach(product -> {
            SupplierProduct supProduct = null;
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
                    supProduct.setProductId(product.getId());
                    result.add(supProduct);
                }
            } else {
                log("supProduct of " + product.getName() + " is incorrect");
            }
        });
        return result;
    }

    @Override
    public List<SupplierProduct> parseProducts() {
        var file = FTPManager.getFileFromSuppliers("MyragToys", ".xls");
        var dataManager = new XlsxDataManager(
                file,
                TableConfig.MiragToysConfig.ARTICLE_COL_NAME
        ) {
            @Override
            public void removeGarbageFromData(List<List<Object>> data) {
                //TODO
            }
        };
        var products = dataManager.parseProductsList(row -> {
                    var idStr = row.get(TableConfig.MiragToysConfig.ID_COL_NAME);
                    var id = idStr == null ? null : (long) Double.parseDouble(idStr);
                    var priceStr = row.get(TableConfig.MiragToysConfig.PRICE_COL_NAME);
                    var price = priceStr == null ? null : Double.parseDouble(priceStr);
                    var name = row.get(TableConfig.MiragToysConfig.NAME_COL_NAME);
                    var brand = row.get(TableConfig.MiragToysConfig.BRAND_COL_NAME);
                    String barcode = null;
                    String article = row.get(TableConfig.MiragToysConfig.ARTICLE_COL_NAME);
//                    try {
//                        article = articleStr == null ? null : String.valueOf((int) Double.parseDouble(articleStr));
//                    } catch (NumberFormatException ignore) {
//                        article = articleStr;
//                    }
                    var stockStr = row.get(TableConfig.MiragToysConfig.STOCKS_COL_NAME);
                    var stock = stockStr == null ? 0 : (int) Double.parseDouble(stockStr);
                    var product = new SupplierProduct();
                    product.setName(name);
                    product.setArticle(article);
                    product.setBrandName(brand);
                    product.setBarcode(barcode);
                    product.setPrice(price);
                    product.setStock(stock);
                    product.setSupplierName(getName());
                    return product;
                }
        );
        return products;
    }

    @Override
    public boolean sendOrders(Collection<? extends Product> products) {
        return false;
    }

}
