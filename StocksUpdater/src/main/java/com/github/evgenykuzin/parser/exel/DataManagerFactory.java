package com.github.evgenykuzin.parser.exel;

import com.github.evgenykuzin.entities.OzonProduct;
import com.github.evgenykuzin.entities.Product;
import com.github.evgenykuzin.entities.XmarketProduct;
import com.github.evgenykuzin.app.cnfg.TableConfig;
import com.github.evgenykuzin.entities.Table;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.github.evgenykuzin.app.cnfg.TableConfig.OzonConfig.*;
import static com.github.evgenykuzin.app.cnfg.TableConfig.WebDocConfig.*;

public class DataManagerFactory {

    public static List<Product> parseOzonProducts(Collection<Table.Row> rows) {
        return rows.stream()
                .map((Function<Table.Row, Product>) row -> {
                        var idStr = row.get(ID_COL_NAME);
                        var id = idStr == null ? null : Integer.parseInt(idStr);
                        var priceStr = row.get(PRICE_COL_NAME);
                        var price = priceStr == null ? null : Double.parseDouble(priceStr);
                        var name = row.get(NAME_COL_NAME);
                        var barcode = row.get(BARCODE_COL_NAME);
                        var article = row.get(ARTICLE_COL_NAME);
                        var href = row.get(CONCURRENT_URL_COL_NAME);
                        var ozonProductId = row.get(OZON_PRODUCT_ID_COL_NAME);
                        var searchBarcode = row.get(SEARCH_BARCODE_COL_NAME);
                        var stocksStr = row.get(STOCKS_COL_NAME);
                        var stocks = Integer.parseInt(stocksStr);
                    return new OzonProduct(id, price, name, barcode, article, href, ozonProductId, searchBarcode, stocks);
                }).collect(Collectors.toList());
    }

    public static List<Product> parseXmarketProducts(Collection<Table.Row> rows) {
        return rows.stream()
                .map((Function<Table.Row, Product>) row -> {
                    var idStr = row.get(TableConfig.XmarketConfig.ID_COL_NAME);
                    var id = idStr == null ? null : Integer.parseInt(idStr);
                    var priceStr = row.get(TableConfig.XmarketConfig.PRICE_COL_NAME);
                    var price = priceStr == null ? null : Double.parseDouble(priceStr);
                    var name = row.get(TableConfig.XmarketConfig.NAME_COL_NAME);
                    var barcode = row.get(TableConfig.XmarketConfig.BARCODE_COL_NAME);
                    var article = row.get(TableConfig.XmarketConfig.ARTICLE_COL_NAME);
                    var stocksStr = row.get(TableConfig.XmarketConfig.STOCKS_COL_NAME);
                    var stocks = Integer.parseInt(stocksStr);
                    return new XmarketProduct(id, price, name, barcode, article, stocks);
                }).collect(Collectors.toList());
    }

    public static WebCsvDataManager getOzonWebCsvManager() {
        return new WebCsvDataManager() {
            @Override
            public List<Product> parseProducts(Collection<Table.Row> rows) {
                return parseOzonProducts(rows);
            }
        };
    }

    public static CsvDataManager getXmarketCsvManager(File inFile) {
        return new CsvDataManager(inFile, inFile, "windows-1251", TableConfig.XmarketConfig.ARTICLE_COL_NAME) {
            @Override
            public List<Product> parseProducts(Collection<Table.Row> rows) {
                return parseXmarketProducts(rows);
            }
        };
    }

    public static CsvDataManager getOzonUpdateCsvManager(File inFile, File outFile) {
        return new CsvDataManager(inFile, outFile, StandardCharsets.UTF_8.toString(), TableConfig.OzonUpdateConfig.ARTICLE_COL_NAME) {
            @Override
            public List<Product> parseProducts(Collection<Table.Row> rows) {
                return parseXmarketProducts(rows);
            }
        };
    }

}
