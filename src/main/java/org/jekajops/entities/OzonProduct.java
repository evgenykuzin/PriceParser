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
    private final int id;
    private final double price;
    private final String name;
    private final String barcode;
    private final String article;
}
