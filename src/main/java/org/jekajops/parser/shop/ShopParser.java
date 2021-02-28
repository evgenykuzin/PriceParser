package org.jekajops.parser.shop;

import org.jekajops.entities.Product;

import java.util.Comparator;
import java.util.List;

public interface ShopParser {
    List<Product> parseProducts(String key);

    default Product getLowerPriceProduct(List<Product> products, Product actualPriceProduct) {
        products.add(actualPriceProduct);
        return products
                .stream()
                .filter(product -> product.getName().contains(actualPriceProduct.getName()))
                .min(Comparator.comparingDouble(Product::getPrice))
                .orElse(actualPriceProduct);
    }

    default Product getLowerPriceProduct(String key, Product actualPriceProduct) {
        return getLowerPriceProduct(parseProducts(key), actualPriceProduct);
    }

    void quit() ;
}
