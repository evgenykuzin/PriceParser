package org.jekajops.parser.exel;

import org.jekajops.entities.OzonProduct;
import org.jekajops.entities.Product;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DataManagerFactory {

    public static List<Product> parseOzonProducts(Collection<Map<String, String>> maps) {
        return maps.stream()
                .map((Function<Map<String, String>, Product>) map -> {
                    var idStr = map.get("id");
                    var id = Integer.parseInt(idStr);
                    var priceStr = map.get("Текущая цена с учетом скидки, руб.");
                    var price = Double.parseDouble(priceStr);
                    var name = map.get("Наименование товара");
                    var barcode = map.get("Barcode");
                    var article = map.get("Артикул");
                    return new OzonProduct(id, price, name, barcode, article);
                }).collect(Collectors.toList());
    }

    public static CsvManager getOzonCsvManager(String filename) {
        return new CsvManager(filename) {
            @Override
            public List<Product> parseProducts(Collection<Map<String, String>> maps) {
                return parseOzonProducts(maps);
            }
        };
    }

    public static WebCsvManager getOzonWebCsvManager() {
        return new WebCsvManager() {
            @Override
            public List<Product> parseProducts(Collection<Map<String, String>> maps) {
                return parseOzonProducts(maps);
            }
        };
    }

}
