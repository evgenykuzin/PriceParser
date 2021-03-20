package com.github.evgenykuzin.parser;

import com.github.evgenykuzin.core.entities.Product;

import java.util.List;

public interface SupplierParser {
    List<Product> parseNewStocksProducts(List<Product> products);
}
