package org.jekajops.parser.exel;

import org.jekajops.entities.Product;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface DataManager {
    Map<String, Map<String, String>> parseMaps();

    void writeAll(Collection<Map<String, String>> maps, String[] colNames);

    List<Product> parseProducts(Collection<Map<String, String>> maps);
}
