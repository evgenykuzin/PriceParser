package com.github.evgenykuzin.worker;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import com.github.evgenykuzin.app.loger.Loggable;
import com.github.evgenykuzin.entities.OzonProduct;
import com.github.evgenykuzin.entities.Table;
import com.github.evgenykuzin.integrate.ozon.OzonManager;
import com.github.evgenykuzin.parser.exel.DataManagerFactory;
import com.github.evgenykuzin.parser.exel.WebCsvDataManager;
import com.github.evgenykuzin.parser.util.XmarketParser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.stream.Collectors;

import static com.github.evgenykuzin.app.cnfg.TableConfig.OzonUpdateConfig.*;

public class StocksUpdater implements Runnable, Loggable {
    @Override
    public void run() {
        log("start updating stocks");
        try {
            log("try");
            updateStocksWithApi();
            log("try ок");
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void updateStocksWithApi() throws IOException, InterruptedException {
        var ozonManager = new OzonManager();
        var dataManager = DataManagerFactory.getOzonWebCsvManager();
        var products = dataManager.parseProducts(dataManager.parseTable().values());
        var updatedStocksProducts = XmarketParser.parseNewStocksProducts(products);
        log("start execute req");
        ozonManager.updateProductStocks(updatedStocksProducts.stream()
                .map(product -> ((OzonProduct) product))
                .collect(Collectors.toList()));
        log("stocks has updated");
    }

    public void updateStocksWithFile(File inFile) throws IOException {
        WebCsvDataManager webCsvDataManager = DataManagerFactory.getOzonWebCsvManager();
        var table = webCsvDataManager.parseTable();
        var products = webCsvDataManager.parseProducts(table.values());
        var updatedStocksProducts = XmarketParser.parseNewStocksProducts(products);
        var patternTable = new Table(ARTICLE_COL_NAME, List.of(
                ARTICLE_COL_NAME,
                NAME_COL_NAME,
                STOCKS_COL_NAME
        ));
        updatedStocksProducts.forEach(product -> {
            Table.Row row = new Table.Row();
            row.put(ARTICLE_COL_NAME, product.getArticle());
            row.put(NAME_COL_NAME, "");
            row.put(STOCKS_COL_NAME, String.valueOf(product.getStock()));
            patternTable.put(product.getArticle(), row);
        });
        writeTable(inFile, patternTable);
    }

    public File writeTable(File file, Table table) throws IOException {
        try {
            var rows = table.getValuesMatrix();
            var wb = new XSSFWorkbook(file);
            try (OutputStream fileOut = new FileOutputStream("workbook.xlsx")) {
                wb.write(fileOut);
            }
            Sheet sheet = wb.getSheetAt(0);
            String name = file.getName();
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
            try (OutputStream fileOut = new FileOutputStream(file)) {
                wb.write(fileOut);
            }
            System.out.println(name + ".xlsx has been generated");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return file;
    }

}