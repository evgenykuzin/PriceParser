package com.github.evgenykuzin.core.entities;

import com.github.evgenykuzin.core.entities.product.Product;
import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.persistence.Table;
import javax.persistence.*;

@FieldDefaults(level = AccessLevel.PRIVATE)
@ToString
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Data
@Entity
@Table(name = "prices")
public class Price {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @OneToOne(cascade = CascadeType.ALL,  fetch = FetchType.EAGER)
    Product product;

    @Column(name = "price")
    Double price;

    @Column(name = "concurrent_price")
    Double concurrentPrice;

    @Column(name = "supplier_price")
    Double supplierPrice;

    @Column(name = "prices_diff")
    Double pricesDiff;

    @Column(name = "concurrent_url")
    String concurrentUrl;

}
