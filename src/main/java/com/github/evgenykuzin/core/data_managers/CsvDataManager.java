package com.github.evgenykuzin.core.data_managers;

import com.github.evgenykuzin.core.entities.Table;
import com.github.evgenykuzin.core.util.loger.Loggable;
import com.github.evgenykuzin.core.util.managers.CsvManager;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@Getter
@Setter
public abstract class CsvDataManager implements DataManager, Loggable {
    private File inFile;
    private File outFile;
    private String charset;
    private String keyColName;
    private char separator;

    @Override
    public Table parseTable() {
        List<List<Object>> data = CsvManager.read(inFile.getAbsolutePath(), charset, separator)
                .stream()
                .map(strings -> Arrays.asList(((Object[]) strings)))
                .collect(Collectors.toList());
        var keys = data.get(0);
        data.remove(keys);
        return new Table(keyColName, keys, data);
    }
    public Table parseTable(List<Object> keys) {
        List<List<Object>> data = CsvManager.read(inFile.getAbsolutePath(), charset, separator)
                .stream()
                .map(strings -> Arrays.asList(((Object[]) strings)))
                .collect(Collectors.toList());
        return new Table(keyColName, keys, data);
    }

    @Override
    public void writeAll(Table table) {
        try(var writer = CsvManager.openWriter(outFile.getAbsolutePath())){
            writer.writeAll(table.getValuesMatrix()
                    .stream()
                    .map(objects -> objects.stream()
                            .map(Object::toString)
                            .toArray(String[]::new))
                    .collect(Collectors.toList())
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
