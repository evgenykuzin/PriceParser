package org.jekajops.entities;

import lombok.Getter;
import org.jekajops.app.loger.Loggable;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Getter
public class Table extends HashMap<String, Table.Row> {
    private final List<String> keys;
    private final String idKeyName;

    public static Table getEmptyTable() {
        return new Table("", Collections.emptyList(), new ArrayList<List<Object>>());
    }

    public Table(String idKeyName, List<Object> keys, Collection<List<Object>> collections, Consumer<Row> beforeRowFiller, Consumer<Row> afterRowFiller) {
        this.idKeyName = idKeyName;
        this.keys = keys.stream().map(Object::toString).collect(toList());
        var rowHashMap = new HashMap<String, Row>();
        collections.stream()
                .map(objects -> {
                    var row = new Row();
                    beforeRowFiller.accept(row);
                    fillBaseRow(row, objects);
                    afterRowFiller.accept(row);
                    return row;
                }).forEach(row -> rowHashMap.put(row.get(idKeyName), row));
        putAll(rowHashMap);
    }

    public Table(String idKeyName, List<Object> keys, Collection<List<Object>> collections) {
        this(idKeyName, keys, collections, row -> { }, row -> { });
    }

    public Table(String idKeyName, List<Object> keys, List<Row> collection) {
        this.idKeyName = idKeyName;
        this.keys = keys.stream().map(Object::toString).collect(toList());
        collection.forEach(row -> put(row.get(idKeyName), row));
    }

    public Table(String idKeyName, List<Object> keys) {
        this.idKeyName = idKeyName;
        this.keys = keys.stream().map(Object::toString).collect(toList());
    }

    private void fillBaseRow(Map<String, String> map, List<Object> objects) {
            for (int i = 0; i < objects.size(); i++) {
                map.put(keys.get(i), objects.get(i).toString());
            }
    }

    public List<List<Object>> getValuesMatrix(Comparator<Row> srt) {
        List<List<Object>> res = new ArrayList<>();
        res.add(keys.stream()
                .map(s -> ((Object) s))
                .collect(Collectors.toList()));
        List<List<Object>> v = values().stream()
                .sorted(srt)
                .map(row -> row.values()
                        .stream()
                        .map(s -> ((Object) s))
                        .collect(Collectors.toList())
                ).collect(toList());
        res.addAll(v);
        return res;
    }

    public List<List<Object>> getValuesMatrix() {
        return getValuesMatrix((o1, o2) -> 0);
    }

    public void updateRawValue(String id, String keyName, String value) {
        var m = get(id);
        m.put(keyName, value);
        put(id, m);
    }

    public static class Row extends LinkedHashMap<String, String> {
    }

    public static class KeysAndValuesNotMatchesSizesException extends Exception {
    }
}
