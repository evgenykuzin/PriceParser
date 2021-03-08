package org.jekajops.parser.exel;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.jekajops.app.loger.Loggable;
import org.jekajops.entities.Table;
import org.jekajops.util.CsvManager;

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

    @Override
    public Table parseTable() {
        List<List<Object>> data = CsvManager.read(inFile.getAbsolutePath(), charset)
                .stream()
                .map(strings -> Arrays.asList(((Object[]) strings)))
                .collect(Collectors.toList());
        var keys = data.get(0);
        data.remove(keys);
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
