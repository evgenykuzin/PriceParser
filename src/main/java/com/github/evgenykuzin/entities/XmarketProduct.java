package com.github.evgenykuzin.entities;

import lombok.*;

@ToString
@EqualsAndHashCode
@AllArgsConstructor
@Getter
@Setter
public class XmarketProduct implements Product {
    private final Integer id;
    private final Double price;
    private final String name;
    private final String barcode;
    private final String article;
    private Integer stock;
}
