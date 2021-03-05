package org.jekajops.entities;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@ToString
@EqualsAndHashCode
@AllArgsConstructor
@Getter
public class OzonProduct implements Product {
    private final Integer id;
    private final Double price;
    private final String name;
    private final String barcode;
    private final String article;
    private final String href;
    private final String ozonProductId;
    private final String searchBarcode;
}
