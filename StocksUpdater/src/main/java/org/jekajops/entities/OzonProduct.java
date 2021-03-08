package org.jekajops.entities;

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
    private final String barcode;
    private final String article;
    private final String concurrentProductUrl;
    private final String ozonProductId;
    private final String searchBarcode;
    private Integer stock;
}
