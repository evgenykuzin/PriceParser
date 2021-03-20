package com.github.evgenykuzin.core.entities;

public interface Product {
    Integer getId();
    Double getPrice();
    String getName();
    String getBrand();
    String getBarcode();
    String getArticle();
    Integer getStock();
    String getSupplier();
    void setStock(Integer stock);
}
