package com.github.evgenykuzin.worker;

import com.github.evgenykuzin.core.data_managers.DataManagerFactory;
import com.github.evgenykuzin.core.entities.Product;
import com.github.evgenykuzin.core.cnfg.OzonConstConfig;
import com.github.evgenykuzin.core.parser.SupplierManager;
import com.github.evgenykuzin.core.util_managers.FileManager;
import com.github.evgenykuzin.parser.OzonSeleniumManager;
import com.github.evgenykuzin.core.parser.ZooekspressParser;
import lombok.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class OzonProductsExistedFilter implements Runnable {
    private final OzonSeleniumManager ozonSeleniumManager;
    private static final File OUTPUT_FILE_FOR_EXISTED = FileManager.getFromResources("existing_ozon_products.txt");
    private static final File OUTPUT_FILE_FOR_NOT_EXISTED = FileManager.getFromResources("not_existing_ozon_products.txt");
    private static final String CHILDREN_PRODUCTS_CATEGORY_SEARCH_URL = "https://www.ozon.ru/search/?page=%d&text=%s&from_global=true&category=" + OzonConstConfig.CategoriesConfig.CHILDREN_CAT;
    private static final double BOTTOM_LIMIT_PRICE = 250.0;

    OzonProductsExistedFilter() {
        ozonSeleniumManager = new OzonSeleniumManager(CHILDREN_PRODUCTS_CATEGORY_SEARCH_URL);
    }

    @Override
    public void run() {
        work(new ZooekspressParser());
    }

    public void work(SupplierManager supplierParser) {
        List<String> alwaysCheckedArticles = new ArrayList<>();

        List<String> existedArticles = FileManager
                .readFile(OUTPUT_FILE_FOR_EXISTED)
                .stream()
                .map(s -> s.isEmpty() ? s : s.split("'")[1])
                .collect(Collectors.toList());

        List<String> notExistedButCheckedArticles = FileManager
                .readFile(OUTPUT_FILE_FOR_NOT_EXISTED)
                .stream()
                .map(s -> s.isEmpty() ? s : s.split("'")[1])
                .collect(Collectors.toList());

        var ozonWebDataManager = DataManagerFactory.getOzonWebCsvManager();
        var webTable = ozonWebDataManager.parseTable();
        var ozonWebProductArticles = ozonWebDataManager
                .parseProducts()
                .stream()
//                .filter(product -> product
//                        .getSupplierName()
//                        .equals(supplierParser.getName())
//                )
                .map(Product::getArticle)
                .collect(Collectors.toList());

        alwaysCheckedArticles.addAll(existedArticles);
        alwaysCheckedArticles.addAll(notExistedButCheckedArticles);
        alwaysCheckedArticles.addAll(ozonWebProductArticles);
        var products = supplierParser.parseProducts();
        products.stream()
                .filter(product -> product.getPrice() > BOTTOM_LIMIT_PRICE)
                .forEach(product -> {
                    var article = product.getArticle();
                    if (!alwaysCheckedArticles.contains(article)) {
                        var brand = product.getBrandName();
                        System.out.println("product = " + product);
                        if (brand != null) {
                            if (brand.contains("\"")) {
                                brand = brand.split("\"")[1];
                            }
                        }
                        var sku = ozonSeleniumManager.parseProductsSKUByBarcode(article, product.getName(), brand);
                        System.out.println("sku = " + sku);
                        var entry = new Entry(article, sku);
                        if (sku != null) {
                            FileManager.writeNextToFile(OUTPUT_FILE_FOR_EXISTED, entry.toString());
                        } else {
                            FileManager.writeNextToFile(OUTPUT_FILE_FOR_NOT_EXISTED, entry.toString());
                        }
                    }

                });
    }

    @AllArgsConstructor
    @Getter
    @Setter
    @EqualsAndHashCode
    private static class Entry {
        private String article;
        private String sku;

        @Override
        public String toString() {
            return "article='" + article + '\'' +
                    ", sku='" + sku + '\'';
        }
    }

    public static void main(String[] args) {
        new Thread(new OzonProductsExistedFilter()).start();
    }
}
