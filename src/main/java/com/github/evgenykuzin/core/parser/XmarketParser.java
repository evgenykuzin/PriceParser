package com.github.evgenykuzin.core.parser;

import com.github.evgenykuzin.core.cnfg.TableConfig;
import com.github.evgenykuzin.core.data_managers.CsvDataManager;
import com.github.evgenykuzin.core.data_managers.DataManagerFactory;
import com.github.evgenykuzin.core.data_managers.WebCsvDataManager;
import com.github.evgenykuzin.core.entities.Product;
import com.github.evgenykuzin.core.entities.SupplierProduct;
import com.github.evgenykuzin.core.entities.Table;
import com.github.evgenykuzin.core.util_managers.FileManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.github.evgenykuzin.core.cnfg.TableConfig.AdditionalOzonDocFieldsConfig.SEARCH_BARCODE_COL_NAME;
import static com.github.evgenykuzin.core.cnfg.TableConfig.AdditionalOzonDocFieldsConfig.SUPPLIER_COL_NAME;
import static com.github.evgenykuzin.core.cnfg.TableConfig.XmarketConfig.BARCODE_COL_NAME;
import static com.github.evgenykuzin.core.cnfg.TableConfig.XmarketConfig.STOCKS_COL_NAME;

public class XmarketParser implements SupplierManager {
    private static final String URL = "https://www.xmarket.ru/";

    @Override
    public String getName() {
        return "Xmarket";
    }

    public List<Product> parseNewStocksProducts(List<Product> products) {
        var tradeTable = getCsvDataManager().parseTable();
        var resultProducts = new ArrayList<Product>();
        products.forEach(product -> {
            var row = tradeTable.get(product.getArticle());
            if (row != null) {
                var stock = row.get(STOCKS_COL_NAME);
                if (stock != null) {
                    var intStck = stock.isEmpty() ? 0 : Integer.parseInt(stock);
                    product.setStock(intStck);
                    resultProducts.add(product);
                }
            }

        });
        return resultProducts;
    }

    @Override
    public List<Product> parseProducts() {
        return getCsvDataManager().parseProductsList(row -> {
            var idStr = row.get(TableConfig.XmarketConfig.ID_COL_NAME);
            var id = idStr == null ? null : Long.parseLong(idStr);
            var priceStr = row.get(TableConfig.XmarketConfig.PRICE_COL_NAME);
            var price = priceStr == null ? null : Double.parseDouble(priceStr);
            var name = row.get(TableConfig.XmarketConfig.NAME_COL_NAME);
            var brand = row.get(TableConfig.XmarketConfig.BRAND_COL_NAME);
            var barcode = row.get(TableConfig.XmarketConfig.BARCODE_COL_NAME);
            var article = row.get(TableConfig.XmarketConfig.ARTICLE_COL_NAME);
            var stocksStr = row.get(TableConfig.XmarketConfig.STOCKS_COL_NAME);
            var stocks = Integer.parseInt(stocksStr);
            return new SupplierProduct(id, idStr, price, name, brand, barcode, article, stocks, getName());
        });
    }

    @Override
    public boolean sendOrders(Collection<? extends Product> products) {
        return false;
    }

    private static CsvDataManager getCsvDataManager() {
        File file = null;
        try {
            file = FileManager.download(
                    URL + "upload/clients/trade.csv",
                    FileManager.getFromResources("trade.csv")
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
        return DataManagerFactory.getCsvManager(file, TableConfig.XmarketConfig.ARTICLE_COL_NAME);
    }

    public void updateBarcodes(WebCsvDataManager webCsvDataManager) {
        Table tradeTable = getCsvDataManager().parseTable();
        Table productsTable = webCsvDataManager.parseTable();
        var products = parseProducts();
        products.forEach(product -> {
            var article = product.getArticle();
            var tradeRaw = tradeTable.get(article);
            if (tradeRaw != null) {
                var barcode = tradeRaw.get(BARCODE_COL_NAME);
                var id = String.valueOf(product.getId());
                productsTable.updateRowValue(id, SEARCH_BARCODE_COL_NAME, barcode);
            }
        });
        webCsvDataManager.writeAll(productsTable);
    }

    public void updateSuppliers(WebCsvDataManager webCsvDataManager) {
        Table productsTable = webCsvDataManager.parseTable();
        var products = parseProducts();
        products.forEach(product -> {
            var article = product.getArticle();
            if (!article.contains("onjoy") && !article.contains("rl-") && !article.contains("RL-")) {
                var id = String.valueOf(product.getId());
                var supplier = getName();
                productsTable.updateRowValue(id, SUPPLIER_COL_NAME, supplier);
            }
        });
        webCsvDataManager.writeAll(productsTable);
    }

}
