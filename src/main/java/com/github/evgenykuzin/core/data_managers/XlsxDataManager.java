package com.github.evgenykuzin.core.data_managers;

import com.github.evgenykuzin.core.entities.Product;
import com.github.evgenykuzin.core.entities.Table;
import lombok.AllArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
public abstract class XlsxDataManager implements DataManager {
    private final File inFile;
    private  final File outFile;
    private final String keyColName;

    @Override
    public Table parseTable() {
        List<List<Object>> data = new ArrayList<>();
        try (var inp = new FileInputStream(inFile)) {
            var wb = WorkbookFactory.create(inp);
            Sheet sheet = wb.getSheetAt(0);
            String name = outFile.getName();
            Header header = sheet.getHeader();
            System.out.println("header = " + header);
            int rowsCount = sheet.getLastRowNum();
            for (int i = 0; i <= rowsCount; i++) {
                List<Object> rowList = new ArrayList<>();
                Row row = sheet.getRow(i);
                int colCounts = row.getLastCellNum();
                for (int j = 0; j < colCounts; j++) {
                    Cell cell = row.getCell(j);
                    rowList.add(cell.toString());
                }
                data.add(rowList);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        var keys = data.get(0);
        data.remove(keys);
        return new Table(keyColName, keys, data);
    }

    @Override
    public void writeAll(Table table) {
        try {
            var rows = table.getValuesMatrix();
            var wb = new XSSFWorkbook(outFile);
            try (OutputStream fileOut = new FileOutputStream(outFile)) {
                wb.write(fileOut);
            }
            Sheet sheet = wb.getSheetAt(0);
            String name = outFile.getName();
            for (int k = 1; k < rows.size(); k++) {
                List<Object> ardata = rows.get(k);
                Row row = sheet.createRow(k);
                for (int p = 0; p < ardata.size(); p++) {
                    System.out.println(ardata.get(p));
                    Cell cell = row.createCell((short) p);
                    cell.setCellValue(ardata.get(p).toString());
                }
            }
            wb.getPackage().flush();
            try (OutputStream fileOut = new FileOutputStream(outFile)) {
                wb.write(fileOut);
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println(name + " has been generated");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
