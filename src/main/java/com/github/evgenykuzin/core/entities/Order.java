package com.github.evgenykuzin.core.entities;

import com.github.evgenykuzin.core.parser.SupplierManager;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.util.Collection;

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class Order {
    Client client;
    Collection<Product> products;
    SupplierManager supplierManager;
}
