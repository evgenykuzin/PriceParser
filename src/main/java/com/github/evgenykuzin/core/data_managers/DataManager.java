package com.github.evgenykuzin.core.data_managers;

import com.github.evgenykuzin.core.entities.Product;
import com.github.evgenykuzin.core.entities.Table;

import java.util.Collection;
import java.util.List;

public interface DataManager {

    Table parseTable();

    void writeAll(Table table);

    List<Product> parseProducts(Collection<Table.Row> maps);

}
