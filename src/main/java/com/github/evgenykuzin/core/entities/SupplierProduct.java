package com.github.evgenykuzin.core.entities;


import lombok.*;

@ToString
@EqualsAndHashCode
@AllArgsConstructor
@Getter
@Setter
public class SupplierProduct implements Product {
    private final Long id;
    private final String supplierId;
    private final Double price;
    private final String name;
    private final String brandName;
    private final String barcode;
    private final String article;
    private Integer stock;
    private final String supplierName;
}