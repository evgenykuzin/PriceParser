package com.github.evgenykuzin.core.entities;

public interface Product {
    Long getId();
    String getSupplierId();
    Double getPrice();
    String getName();
    String getBrandName();
    String getBarcode();
    String getArticle();
    Integer getStock();
    String getSupplierName();
    void setStock(Integer stock);
}
