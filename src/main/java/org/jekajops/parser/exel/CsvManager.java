package org.jekajops.parser.exel;

import com.opencsv.*;
import com.opencsv.exceptions.CsvException;
import org.jekajops.app.cnfg.AppConfig;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.jekajops.app.cnfg.AppConfig.logger;

public abstract class CsvManager implements DataManager {
    private String filename;

    public CsvManager(String filename) {
        this.filename = filename;
        log("filename = " + filename);
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public Map<String, Map<String, String>> parseMaps() {
        List<String[]> list = read();
        var keys = list.get(0);
        list.remove(keys);
        var indices = new ArrayList<Integer>();
        for (int i = 1; i < list.size() + 1; i++) {
            indices.add(i);
        }
        Iterator<Integer> indicesIterator = indices.iterator();
        return list
                .stream()
                .map((Function<String[], Map<String, String>>) values -> {
                    var map = new LinkedHashMap<String, String>();
                    map.putIfAbsent("id", String.valueOf(indicesIterator.next()));
                    for (int i = 0; i < values.length; i++) {
                        map.put(keys[i].replaceAll("\uFEFF", ""), values[i]);
                    }
                    map.putIfAbsent(AppConfig.NEW_PRICE_COL_NAME, "");
                    map.putIfAbsent(AppConfig.DIFF_PRICES_COL_NAME, "");
                    return map;
                })
                .collect(Collectors.toMap(map -> map.get("id"), map -> map));
    }

    public void writeAll(Collection<Map<String, String>> maps, String[] colNames) {
        var collection = new ArrayList<String[]>();
        collection.add(colNames);
        collection.addAll(maps.stream()
                .map(map -> map.values().toArray(String[]::new))
                .collect(Collectors.toList())
        );
        try(ICSVWriter writer = openWriter()) {
            writer.writeAll(collection);
        } catch (FileNotFoundException ignored) {
            log("waiting until user close exel");
        } catch (IOException e) {
            e.printStackTrace();
            log(e.getMessage());
        }
    }

    private void log(String msg) {
        logger.log(msg);
    }

    private List<String[]> read() {
        CSVParser csvParser = new CSVParserBuilder().withSeparator(';').build();
        try (CSVReader reader = new CSVReaderBuilder(new InputStreamReader(new FileInputStream(filename), StandardCharsets.UTF_8))
                .withCSVParser(csvParser)
                .build()) {
            return reader.readAll();
        } catch (IOException | CsvException e) {
            e.printStackTrace();
            log(e.getMessage());

        }
        return Collections.emptyList();
    }

    private ICSVWriter openWriter() throws IOException {
        CSVParser csvParser = new CSVParserBuilder().withSeparator(';').build();
            OutputStream os = new FileOutputStream(filename);
            os.write(239);
            os.write(187);
            os.write(191);
            return new CSVWriterBuilder(new OutputStreamWriter(os, StandardCharsets.UTF_8))
                    .withParser(csvParser)
                    .build();

    }



}
