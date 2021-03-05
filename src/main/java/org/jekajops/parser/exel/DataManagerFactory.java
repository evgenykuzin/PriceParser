package org.jekajops.parser.exel;

import org.jekajops.entities.OzonProduct;
import org.jekajops.entities.Product;
import org.jekajops.entities.Table;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.jekajops.app.cnfg.TableConfig.*;
import static org.jekajops.app.cnfg.TableConfig.OzonConfig.*;

public class DataManagerFactory {

    public static List<Product> parseOzonProducts(Collection<Table.Raw> raws) {
        return raws.stream()
                .map((Function<Table.Raw, Product>) raw -> {
                        var idStr = raw.get(ID_COL_NAME);
                        var id = idStr == null ? null : Integer.parseInt(idStr);
                        var priceStr = raw.get(PRICE_COL_NAME);
                        var price = priceStr == null ? null : Double.parseDouble(priceStr);
                        var name = raw.get(NAME_COL_NAME);
                        var barcode = raw.get(BARCODE_COL_NAME);
                        var article = raw.get(ARTICLE_COL_NAME);
                        var href = raw.get(HREF_COL_NAME);
                        var ozonProductId = raw.get(OZON_PRODUCT_ID);
                        var searchBarcode = raw.get(SEARCH_BARCODE_COL_NAME);
                        return new OzonProduct(id, price, name, barcode, article, href, ozonProductId, searchBarcode);
                }).collect(Collectors.toList());
    }

    public static WebCsvDataManager getOzonWebCsvManager() {
        return new WebCsvDataManager() {
            @Override
            public List<Product> parseProducts(Collection<Table.Raw> raws) {
                return parseOzonProducts(raws);
            }
        };
    }

}
