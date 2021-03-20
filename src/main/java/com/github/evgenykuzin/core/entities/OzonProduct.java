package com.github.evgenykuzin.core.entities;

import lombok.*;

@ToString
@EqualsAndHashCode
@AllArgsConstructor
@Getter
@Setter
public class OzonProduct implements Product {
    private final Integer id;
    private final Double price;
    private final String name;
    private final String brand;
    private final String barcode;
    private final String article;
    private final String concurrentProductUrl;
    private final String ozonProductId;
    private final String searchBarcode;
    private Integer stock;
    private final String supplier;

}
