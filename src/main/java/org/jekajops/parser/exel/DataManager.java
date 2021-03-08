package org.jekajops.parser.exel;

import org.jekajops.entities.Product;
import org.jekajops.entities.Table;

import java.util.*;

public interface DataManager {

    Table parseTable();

    void writeAll(Table table);

    List<Product> parseProducts(Collection<Table.Row> maps);

}
