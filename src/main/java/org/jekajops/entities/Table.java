package org.jekajops.entities;

import lombok.Getter;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Getter
public class Table extends HashMap<String, Table.Raw> {
    private final List<String> keys;
    private final String idKeyName;

    public static Table getEmptyTable() {
        return new Table("", Collections.emptyList(), new ArrayList<List<Object>>());
    }

    public Table(String idKeyName, List<Object> keys, Collection<List<Object>> collections, Consumer<Raw> beforeRawFiller, Consumer<Raw> afterRawFiller) {
        this.idKeyName = idKeyName;
        this.keys = keys.stream().map(Object::toString).collect(toList());
        var rawMap = new HashMap<String, Raw>();
        collections.stream()
                .map(objects -> {
                    var map = new Raw();
                    beforeRawFiller.accept(map);
                    fillBaseRaw(map, objects);
                    afterRawFiller.accept(map);
                    return map;
                }).forEach(raw -> rawMap.put(raw.get(idKeyName), raw));
        putAll(rawMap);
    }

    public Table(String idKeyName, List<Object> keys, Collection<List<Object>> collections) {
        this(idKeyName, keys, collections, raw -> { }, raw -> { });
    }

    public Table(String idKeyName, List<Object> keys, List<Raw> collection) {
        this.idKeyName = idKeyName;
        this.keys = keys.stream().map(Object::toString).collect(toList());
        collection.forEach(raw -> put(raw.get(idKeyName), raw));
    }

    private void fillBaseRaw(Map<String, String> map, List<Object> objects) {
            for (int i = 0; i < objects.size(); i++) {
                map.put(keys.get(i), objects.get(i).toString());
            }
    }

    public List<List<Object>> getValuesMatrix(Comparator<Raw> srt) {
        List<List<Object>> res = new ArrayList<>();
        res.add(keys.stream()
                .map(s -> ((Object) s))
                .collect(Collectors.toList()));
        List<List<Object>> v = values().stream()
                .sorted(srt)
                .map(raw -> raw.values()
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

    public static class Raw extends LinkedHashMap<String, String> {
    }

    public static class KeysAndValuesNotMatchesSizesException extends Exception {
    }
}
