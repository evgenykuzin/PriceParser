package com.github.evgenykuzin.core.parser;

import com.github.evgenykuzin.core.api_integrations.ozon.OzonManager;
import com.github.evgenykuzin.core.cnfg.TableConfig;
import com.github.evgenykuzin.core.data_managers.XlsxDataManager;
import com.github.evgenykuzin.core.entities.Product;
import com.github.evgenykuzin.core.entities.SupplierProduct;
import com.github.evgenykuzin.core.util.loger.Loggable;
import com.github.evgenykuzin.core.util_managers.FileManager;
import com.github.evgenykuzin.core.util_managers.MailManager;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ZooekspressParser implements SupplierManager, Loggable {
    private static final int STOCK_CONST = 3;
    private static final String ORDER_STOCK_COL_NAME = "Заказ, шт.";
    private static final File mainFile =
            FileManager.getFromResources("zooekspress.xls");
    private File orderFile;

    @Override
    public String getName() {
        return "Zooekspress";
    }

    @Override
    public List<Product> parseNewStocksProducts(List<Product> products) {
        var supProducts = parseProducts();
        for (Product product : products) {
            if (product.getArticle() == null) continue;
            for (Product supProduct : supProducts) {
                if (supProduct.getArticle() == null) continue;
                if (supProduct.getArticle().equals(product.getArticle())) {
                    product.setStock(STOCK_CONST);
                }
            }
        }
        return products;
    }

    @Override
    public List<Product> parseProducts() {
        File file = MailManager.getImapMailManager().downloadFileFromSuppliers("@zooexpress.ru", "прайс", null, MailManager.PRICES_FOLDER);
        copyFiles(file, mainFile);
        var dataManager = new XlsxDataManagerImpl(
                mainFile,
                TableConfig.ZooekspressConfig.ARTICLE_COL_NAME
        );
        return dataManager.parseProductsList(row -> {
                    var idStr = row.get(TableConfig.ZooekspressConfig.ID_COL_NAME);
                    var id = idStr == null ? null : (long) Double.parseDouble(idStr);
                    var priceStr = row.get(TableConfig.ZooekspressConfig.PRICE_COL_NAME);
                    var price = priceStr == null ? null : Double.parseDouble(priceStr);
                    var name = row.get(TableConfig.ZooekspressConfig.NAME_COL_NAME);
                    var article = row.get(TableConfig.ZooekspressConfig.ARTICLE_COL_NAME);
                    Integer stocks = null;
                    String brand = null;
                    String barcode = null;
                    return new SupplierProduct(id, idStr, price, name, brand, barcode, article, stocks, getName());
                }
        );
    }

    @Override
    public boolean sendOrders(Collection<? extends Product> products) {
        if (products.isEmpty()) {
            log("orders is empty");
            return false;
        }
        orderFile = FileManager.getFromResources("zooekspress_order.xls");
        copyFiles(mainFile, orderFile);
        Workbook wb;
        try (var inp = new FileInputStream(orderFile)) {
            wb = WorkbookFactory.create(inp);
            try {
                if (wb == null) {
                    log("Failed to write to xls file");
                    return false;
                }
                Sheet sheet = wb.getSheetAt(0);
                for (var product : products) {
                    for (int k = 1; k < sheet.getLastRowNum(); k++) {
                        var row = sheet.getRow(k);
                        if (row == null) continue;
                        Cell articleCell = row.getCell(0);
                        if (product == null || articleCell == null) continue;
                        if (articleCell.toString().equals(product.getArticle())) {
                            Cell orderCell = row.getCell(7);
                            if (orderCell == null) {
                                orderCell = row.createCell(7);
                            }
                            String currOrderStr = orderCell.toString();
                            if (!currOrderStr.isEmpty()) {
                                double currOrder = Integer.parseInt(currOrderStr);
                                double incOrder = currOrder + 1.0;
                                orderCell.setCellValue(incOrder);
                            } else orderCell.setCellValue(1.0);
                        }
                    }
                }
                try (OutputStream fileOut = new FileOutputStream(orderFile)) {
                    wb.write(fileOut);
                    log("zooekspress order was generated");
                }
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            } finally {
                if (wb != null) {
                    wb.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public File getOrderFile() {
        return orderFile;
    }

    private static void copyFiles(File source, File target) {
        try (var fos = new FileOutputStream(target)) {
            Files.copy(source.toPath(), fos);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class XlsxDataManagerImpl extends XlsxDataManager {
        public XlsxDataManagerImpl(File inFile, String keyColName) {
            super(inFile, keyColName);
        }

        public XlsxDataManagerImpl(File inFile, File outFile, String keyColName) {
            super(inFile, outFile, keyColName);
        }

        @Override
        public void removeGarbageFromData(List<List<Object>> data) {
            var temp = new ArrayList<>(data);
            boolean breakFlag = false;
            for (var row : temp) {
                if (row.size() > 0) {
                    if (!breakFlag && !row.get(0).equals(TableConfig.ZooekspressConfig.ARTICLE_COL_NAME)) {
                        data.remove(row);
                    } else {
                        breakFlag = true;
                    }
                }
            }
            for (var row : temp) {
                if (row.isEmpty()) {
                    data.remove(row);
                }
            }
        }
    }

    public static void main(String[] args) {
        var z = new ZooekspressParser();
        var op = new OzonManager().getOrderedProducts();
        System.out.println("op = " + op);
        z.sendOrders(op);
    }
}
