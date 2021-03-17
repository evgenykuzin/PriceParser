package com.github.evgenykuzin.parser.exel;

import com.github.evgenykuzin.entities.Product;
import com.github.evgenykuzin.entities.Table;

import java.util.Collection;
import java.util.List;

public interface DataManager {

    Table parseTable();

    void writeAll(Table table);

    List<Product> parseProducts(Collection<Table.Row> maps);

}
