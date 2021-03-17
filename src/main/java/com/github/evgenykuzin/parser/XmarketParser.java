package com.github.evgenykuzin.parser;

import com.github.evgenykuzin.core.data_managers.CsvDataManager;
import com.github.evgenykuzin.core.data_managers.DataManagerFactory;
import com.github.evgenykuzin.core.data_managers.WebCsvDataManager;
import com.github.evgenykuzin.core.entities.Product;
import com.github.evgenykuzin.core.entities.Table;
import com.github.evgenykuzin.core.util.cnfg.TableConfig;
import com.github.evgenykuzin.core.util.managers.FileManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.github.evgenykuzin.core.util.cnfg.TableConfig.AdditionalOzonDocFieldsConfig.SEARCH_BARCODE_COL_NAME;
import static com.github.evgenykuzin.core.util.cnfg.TableConfig.AdditionalOzonDocFieldsConfig.SUPPLIER_COL_NAME;
import static com.github.evgenykuzin.core.util.cnfg.TableConfig.XmarketConfig.BARCODE_COL_NAME;
import static com.github.evgenykuzin.core.util.cnfg.TableConfig.XmarketConfig.STOCKS_COL_NAME;

public class XmarketParser implements SupplierParser {
    private static final String URL = "https://www.xmarket.ru/";

    public List<Product> parseNewStocksProducts(List<Product> products) {
        var tradeTable = getTradeTable();
        var resultProducts = new ArrayList<Product>();
        products.forEach(product -> {
            var row = tradeTable.get(product.getArticle());
            if (row != null) {
                var stock = row.get(STOCKS_COL_NAME);
                if (stock != null && !stock.isEmpty()) {
                    var intStck = Integer.parseInt(stock);
                    intStck = intStck > 0 ? intStck -1 : 0;
                    product.setStock(intStck);
                    resultProducts.add(product);
                }
            }

        });
        return resultProducts;
    }

    private static Table getTradeTable() {
        File file = null;
        try {
            file = FileManager.download(
                    URL + "upload/clients/trade.csv",
                    FileManager.getFromResources("trade.csv")
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
        CsvDataManager csvDataManager = DataManagerFactory.getXmarketCsvManager(file);
        return csvDataManager.parseTable();
    }

    public static void updateBarcodes(WebCsvDataManager dataManager) {
        Table tradeTable = getTradeTable();
        Table productsTable = dataManager.parseTable();
        var products = dataManager.parseProducts(productsTable.values());
        products.forEach(product -> {
            var article = product.getArticle();
            var tradeRaw = tradeTable.get(article);
            if (tradeRaw != null) {
                var barcode = tradeRaw.get(BARCODE_COL_NAME);
                var id = String.valueOf(product.getId());
                productsTable.updateRowValue(id, SEARCH_BARCODE_COL_NAME, barcode);
            }
        });
        dataManager.writeAll(productsTable);
    }

    public static void updateSuppliers(WebCsvDataManager dataManager) {
        Table productsTable = dataManager.parseTable();
        var products = dataManager.parseProducts(productsTable.values());
        products.forEach(product -> {
            var article = product.getArticle();
            if (!article.contains("onjoy") && !article.contains("rl-") && !article.contains("RL-")) {
                var id = String.valueOf(product.getId());
                var supplier = TableConfig.SuppliersNamesConfig.XmarketSupplierConst;
                productsTable.updateRowValue(id, SUPPLIER_COL_NAME, supplier);
            }
        });
        dataManager.writeAll(productsTable);
    }

}
