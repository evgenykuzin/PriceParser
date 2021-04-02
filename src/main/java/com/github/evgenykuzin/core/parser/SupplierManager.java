package com.github.evgenykuzin.core.parser;

import com.github.evgenykuzin.core.entities.Product;

import java.util.Collection;
import java.util.List;

public interface SupplierManager {
    String getName();
    List<Product> parseNewStocksProducts(List<Product> products);
    List<Product> parseProducts();
    boolean sendOrders(Collection<? extends Product> products);
}
