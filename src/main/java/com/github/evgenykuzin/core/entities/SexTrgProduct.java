package com.github.evgenykuzin.core.entities;

import lombok.*;

@ToString
@EqualsAndHashCode
@AllArgsConstructor
@Getter
@Setter
public class SexTrgProduct implements Product {
    private final Integer id;
    private final Double price;
    private final String name;
    private final String barcode;
    private final String article;
    private Integer stock;
    private final String supplier;
}
