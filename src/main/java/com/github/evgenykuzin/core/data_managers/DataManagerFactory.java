package com.github.evgenykuzin.core.data_managers;

import com.github.evgenykuzin.core.entities.*;
import com.github.evgenykuzin.core.util.cnfg.TableConfig;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.github.evgenykuzin.core.util.cnfg.TableConfig.SuppliersNamesConfig.SexTrgSupplierConst;
import static com.github.evgenykuzin.core.util.cnfg.TableConfig.SuppliersNamesConfig.XmarketSupplierConst;

public class DataManagerFactory {

    public static List<Product> parseOzonProducts(Collection<Table.Row> rows) {
        return rows.stream()
                .map((Function<Table.Row, Product>) row -> {
                        var idStr = row.get(TableConfig.AdditionalOzonDocFieldsConfig.ID_COL_NAME);
                        var id = idStr == null || idStr.isEmpty() ? null : Integer.parseInt(idStr);
                        var priceStr = row.get(TableConfig.OzonDocConfig.PRICE_COL_NAME);
                        var price = priceStr == null ? null : Double.parseDouble(priceStr);
                        var name = row.get(TableConfig.OzonDocConfig.NAME_COL_NAME);
                        var barcode = row.get(TableConfig.OzonDocConfig.BARCODE_COL_NAME);
                        var article = row.get(TableConfig.OzonDocConfig.ARTICLE_COL_NAME);
                        var href = row.get(TableConfig.AdditionalOzonDocFieldsConfig.CONCURRENT_URL_COL_NAME);
                        var ozonProductId = row.get(TableConfig.OzonDocConfig.OZON_PRODUCT_ID_COL_NAME);
                        var searchBarcode = row.get(TableConfig.AdditionalOzonDocFieldsConfig.SEARCH_BARCODE_COL_NAME);
                        var stocksStr = row.get(TableConfig.OzonDocConfig.STOCKS_COL_NAME);
                        var stocks = Integer.parseInt(stocksStr);
                        var supplier = row.get(TableConfig.AdditionalOzonDocFieldsConfig.SUPPLIER_COL_NAME);
                    return new OzonProduct(id, price, name, barcode, article, href, ozonProductId, searchBarcode, stocks, supplier);
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
                    return new XmarketProduct(id, price, name, barcode, article, stocks, XmarketSupplierConst);
                }).collect(Collectors.toList());
    }

    public static List<Product> parseSexTrgProducts(Collection<Table.Row> rows) {
        var r = new ArrayList<>(rows);
        r.remove(0);
        return r.stream()
                .map((Function<Table.Row, Product>) row -> {
                    var idStr = row.get(TableConfig.SexTrgConfig.ID_COL_NAME);
                    var id = idStr == null ? null : Integer.parseInt(idStr);
                    //var priceStr = row.get(TableConfig.SexTrgConfig.PRICE_COL_NAME);
                    //var price = priceStr == null ? null : Double.parseDouble(priceStr);
                    var name = row.get(TableConfig.SexTrgConfig.NAME_COL_NAME);
                    var barcode = row.get(TableConfig.SexTrgConfig.BARCODE_COL_NAME);
                    var article = row.get(TableConfig.SexTrgConfig.ARTICLE_COL_NAME);
                    var stocksStr = row.get(TableConfig.SexTrgConfig.STOCKS_COL_NAME);
                    var stocks = Integer.parseInt(stocksStr);
                    return new SexTrgProduct(id, 1.1, name, barcode, article, stocks, SexTrgSupplierConst);
                }).collect(Collectors.toList());
    }

    public static WebCsvDataManager getOzonWebCsvManager(String credentialsFilePath) {
        return new WebCsvDataManager(credentialsFilePath) {
            @Override
            public List<Product> parseProducts(Collection<Table.Row> rows) {
                return parseOzonProducts(rows);
            }
        };
    }

    public static CsvDataManager getXmarketCsvManager(File inFile) {
        return new CsvDataManager(inFile, inFile, "windows-1251", TableConfig.XmarketConfig.ARTICLE_COL_NAME, ';') {
            @Override
            public List<Product> parseProducts(Collection<Table.Row> rows) {
                return parseXmarketProducts(rows);
            }
        };
    }

    public static CsvDataManager getSexTrgCsvManager(File inFile) {
        return new CsvDataManager(inFile, inFile, "windows-1251", TableConfig.SexTrgConfig.ARTICLE_COL_NAME, ';') {
            @Override
            public List<Product> parseProducts(Collection<Table.Row> rows) {
                return parseSexTrgProducts(rows);
            }
        };
    }

    public static CsvDataManager getOzonUpdateCsvManager(File inFile, File outFile) {
        return new CsvDataManager(inFile, outFile, StandardCharsets.UTF_8.toString(), TableConfig.OzonUpdateConfig.ARTICLE_COL_NAME, ';') {
            @Override
            public List<Product> parseProducts(Collection<Table.Row> rows) {
                return parseXmarketProducts(rows);
            }
        };
    }

    public static void main(String[] args) {
        var m = getOzonWebCsvManager("/credentials.json");
        var t = m.parseTable();

    }

}
