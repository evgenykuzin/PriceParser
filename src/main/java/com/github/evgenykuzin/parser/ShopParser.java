package com.github.evgenykuzin.parser;

import com.github.evgenykuzin.core.entities.Price;
import com.github.evgenykuzin.core.entities.product.OzonProduct;
import com.github.evgenykuzin.core.entities.product.Product;

import java.util.Comparator;
import java.util.List;

public interface ShopParser {

    List<OzonProduct> parseProducts(Price price, String key);

    void quit() ;

}