package com.github.evgenykuzin.entities;

public interface Product {
    Integer getId();
    Double getPrice();
    String getName();
    String getBarcode();
    String getArticle();
    Integer getStock();
    void setStock(Integer stock);
}
