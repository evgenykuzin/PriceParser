package com.github.evgenykuzin.core.data_managers;

import com.github.evgenykuzin.core.entities.*;
import com.github.evgenykuzin.core.util.cnfg.TableConfig;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.github.evgenykuzin.core.util.cnfg.TableConfig.SuppliersNamesConfig.*;

public class DataManagerFactory {

    public static List<Product> parseProductsList(Collection<Table.Row> rows, Function<Table.Row, Product> mapFunction) {
        return rows.stream()
                .map(mapFunction)
                .collect(Collectors.toList());
    }

    public static WebCsvDataManager getOzonWebCsvManager() {
        return new WebCsvDataManager("credentials.json") {
            @Override
            public List<Product> parseProducts(Collection<Table.Row> rows) {
                return parseProductsList(rows, row -> {
                    var idStr = row.get(TableConfig.AdditionalOzonDocFieldsConfig.ID_COL_NAME);
                    var id = idStr == null || idStr.isEmpty() ? null : Integer.parseInt(idStr);
                    var priceStr = row.get(TableConfig.OzonDocConfig.PRICE_COL_NAME);
                    var price = priceStr == null ? null : Double.parseDouble(priceStr);
                    var name = row.get(TableConfig.OzonDocConfig.NAME_COL_NAME);
                    var brand = row.get(TableConfig.OzonDocConfig.BRAND_COL_NAME);
                    var barcode = row.get(TableConfig.OzonDocConfig.BARCODE_COL_NAME);
                    var article = row.get(TableConfig.OzonDocConfig.ARTICLE_COL_NAME);
                    var href = row.get(TableConfig.AdditionalOzonDocFieldsConfig.CONCURRENT_URL_COL_NAME);
                    var ozonProductId = row.get(TableConfig.OzonDocConfig.OZON_PRODUCT_ID_COL_NAME);
                    var searchBarcode = row.get(TableConfig.AdditionalOzonDocFieldsConfig.SEARCH_BARCODE_COL_NAME);
                    var stocksStr = row.get(TableConfig.OzonDocConfig.STOCKS_COL_NAME);
                    var stocks = Integer.parseInt(stocksStr);
                    var supplier = row.get(TableConfig.AdditionalOzonDocFieldsConfig.SUPPLIER_COL_NAME);
                    return new OzonProduct(id, price, name, brand, barcode, article, href, ozonProductId, searchBarcode, stocks, supplier);
                });
            }
        };
    }

    public static CsvDataManager getXmarketCsvManager(File inFile) {
        return new CsvDataManager(inFile, inFile, "windows-1251", TableConfig.XmarketConfig.ARTICLE_COL_NAME, ';') {
            @Override
            public List<Product> parseProducts(Collection<Table.Row> rows) {
                return parseProductsList(rows, row -> {
                    var idStr = row.get(TableConfig.XmarketConfig.ID_COL_NAME);
                    var id = idStr == null ? null : Integer.parseInt(idStr);
                    var priceStr = row.get(TableConfig.XmarketConfig.PRICE_COL_NAME);
                    var price = priceStr == null ? null : Double.parseDouble(priceStr);
                    var name = row.get(TableConfig.XmarketConfig.NAME_COL_NAME);
                    var brand = row.get(TableConfig.XmarketConfig.BRAND_COL_NAME);
                    var barcode = row.get(TableConfig.XmarketConfig.BARCODE_COL_NAME);
                    var article = row.get(TableConfig.XmarketConfig.ARTICLE_COL_NAME);
                    var stocksStr = row.get(TableConfig.XmarketConfig.STOCKS_COL_NAME);
                    var stocks = Integer.parseInt(stocksStr);
                    return new SupplierProduct(id, price, name, brand, barcode, article, stocks, XmarketSupplierConst);

                });
            }
        };
    }

    public static CsvDataManager getSexTrgCsvManager(File inFile, File pricesFile) {
        return new CsvDataManager(inFile, inFile, "windows-1251", TableConfig.SexTrgConfig.ARTICLE_COL_NAME, ';') {
            @Override
            public List<Product> parseProducts(Collection<Table.Row> rows) {
                var priceRow = new Table.Row();
                priceRow.put(TableConfig.SexTrgConfig.PRICE_COL_NAME, "");
                rows.add(priceRow);
                return parseProductsList(rows, row -> {
                    var idStr = row.get(TableConfig.SexTrgConfig.ID_COL_NAME);
                    var id = idStr == null ? null : Integer.parseInt(idStr);
                    var priceStr = row.get(TableConfig.SexTrgConfig.PRICE_COL_NAME);
                    var price = priceStr == null ? null : Double.parseDouble(priceStr);
                    var name = row.get(TableConfig.SexTrgConfig.NAME_COL_NAME);
                    var brand = row.get(TableConfig.SexTrgConfig.BRAND_COL_NAME);
                    var barcode = row.get(TableConfig.SexTrgConfig.BARCODE_COL_NAME);
                    var article = row.get(TableConfig.SexTrgConfig.ARTICLE_COL_NAME);
                    var stocksStr = row.get(TableConfig.SexTrgConfig.STOCKS_COL_NAME);
                    var stocks = Integer.parseInt(stocksStr);
                    return new SupplierProduct(id, price, name, brand, barcode, article, stocks, SexTrgSupplierConst);
                });
            }
        };
    }

    public static CsvDataManager getSportOptomCsvManager(File inFile) {
        return new CsvDataManager(inFile, inFile, "windows-1251", TableConfig.SportOptomConfig.ARTICLE_COL_NAME, ';') {
            @Override
            public List<Product> parseProducts(Collection<Table.Row> rows) {
                return parseProductsList(rows, row -> {
                    var idStr = row.get(TableConfig.SportOptomConfig.ID_COL_NAME);
                    var id = idStr == null ? null : Integer.parseInt(idStr);
                    var priceStr = row.get(TableConfig.SportOptomConfig.PRICE_COL_NAME);
                    var price = priceStr == null ? null : Double.parseDouble(priceStr);
                    var name = row.get(TableConfig.SportOptomConfig.NAME_COL_NAME);
                    var brand = row.get(TableConfig.SportOptomConfig.BRAND_COL_NAME);
                    var barcode = row.get(TableConfig.SportOptomConfig.BARCODE_COL_NAME);
                    var article = row.get(TableConfig.SportOptomConfig.ARTICLE_COL_NAME);
                    var stocksStr = row.get(TableConfig.SportOptomConfig.STOCKS_COL_NAME);
                    var stocks = Integer.parseInt(stocksStr);
                    return new SupplierProduct(id, price, name, brand, barcode, article, stocks, SportOptomSupplierConst);
                });
            }
        };
    }

    public static XlsxDataManager getMyragToysDataManager(File inFile) {
        return new XlsxDataManager(inFile, inFile, TableConfig.MiragToysConfig.ARTICLE_COL_NAME) {
            @Override
            public List<Product> parseProducts(Collection<Table.Row> rows) {
                return parseProductsList(rows, row -> {
                    var idStr = row.get(TableConfig.MiragToysConfig.ID_COL_NAME);
                    var id = idStr == null ? null : (int) Double.parseDouble(idStr);
                    var priceStr = row.get(TableConfig.MiragToysConfig.PRICE_COL_NAME);
                    var price = priceStr == null ? null : Double.parseDouble(priceStr);
                    var name = row.get(TableConfig.MiragToysConfig.NAME_COL_NAME);
                    var brand = row.get(TableConfig.MiragToysConfig.BRAND_COL_NAME);
                    var barcodeStr = row.get(TableConfig.MiragToysConfig.BARCODE_COL_NAME);
                    var barcode = barcodeStr == null ? null : String.valueOf((int) Double.parseDouble(barcodeStr));
                    var articleStr = row.get(TableConfig.MiragToysConfig.ARTICLE_COL_NAME);
                    String article;
                    try {
                        article = articleStr == null ? null : String.valueOf((int) Double.parseDouble(articleStr));
                    } catch (NumberFormatException ignore) {
                        article = articleStr;
                    }
                    var stocksStr = row.get(TableConfig.MiragToysConfig.STOCKS_COL_NAME);
                    var stocks = stocksStr == null ? null : (int) Double.parseDouble(stocksStr);
                    return new SupplierProduct(id, price, name, brand, barcode, article, stocks, MiragToysSupplierConst);
                });
            }
        };
    }

}
