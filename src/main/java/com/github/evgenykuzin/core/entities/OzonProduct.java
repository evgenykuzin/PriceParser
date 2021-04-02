package com.github.evgenykuzin.core.entities;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@ToString
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "ozon_products")
public class OzonProduct implements Product, Serializable {
    private static final Long serialVersionUID = 176798L;

    @Id
    @Column(name = "id")
    private Long id;

    private String supplierId;

    @Column(name = "product_name")
    private String name;

    @Column(name = "article")
    private String article;

    @Column(name = "barcode")
    private String barcode;

    @Column(name = "search_barcode")
    private String searchBarcode;

    @Column(name = "price")
    private Double price;

    @Column(name = "sku_fbs")
    private String skuFbs;

    @Column(name = "sku_fbo")
    private String skuFbo;

    @Column(name = "stock")
    private Integer stock;

    @Column(name = "brand_name")
    private String brandName;

    @Column(name = "supplier_name")
    private String supplierName;

    @Column(name = "category_id")
    private Long categoryId;

    @Column(name = "concurrent_price")
    private Double concurrentPrice;

    @Column(name = "concurrent_product_url")
    private String concurrentProductUrl;

}
