package com.github.evgenykuzin.core.parser;

import com.github.evgenykuzin.core.entities.Product;

import java.util.*;
import java.util.stream.Collectors;

public class SupplierParsersFactory {
    private static Collection<SupplierManager> supplierManagers;

    public static Collection<SupplierManager> getSupplierParsers() {
        if (supplierManagers == null) {
            supplierManagers = List.of(
                    new ZooekspressParser(),
                    new MyragToys(),
                    new XmarketParser()
            );
        }
        return supplierManagers;
    }

    public static void main(String[] args) {
        Map<String, List<String>> map = new HashMap<>();
        for (var sup : getSupplierParsers()) {
            var ps = sup.parseProducts();
                for (Product p : ps) {
                    var article = p.getArticle();
                    var productsV = map.get(article);
                    if (productsV != null && !productsV.isEmpty()) {
                        productsV = new ArrayList<>(productsV);
                        productsV.add(p.toString());
                    } else {
                        productsV = Collections.singletonList(p.toString());
                    }
                    map.put(article, productsV);
                }
        }
        System.out.println("map = " + map.entrySet().stream().filter(entry -> entry.getValue().size() > 1).collect(Collectors.toList()));
    }

}
