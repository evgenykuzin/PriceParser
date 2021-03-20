package com.github.evgenykuzin.parser;

import com.github.evgenykuzin.core.entities.Product;
import com.github.evgenykuzin.core.util.loger.Loggable;
import java.util.List;

public class OzonParserSe implements ShopParser, Loggable {
    private static final String URL = "https://www.ozon.ru/search/?page=%d&text=%s&from_global=true";
    private final OzonSeleniumManager ozonSeleniumManager;
    public OzonParserSe() {
        this.ozonSeleniumManager = new OzonSeleniumManager(URL);
    }

    @Override
    public List<Product> parseProducts(String barcode) {
        return ozonSeleniumManager.parseProductsPricesByBarcode(barcode);
    }

    @Override
    public void quit() {
        ozonSeleniumManager.quit();
    }

    @Override
    protected void finalize() throws Throwable {
        ozonSeleniumManager.finalize();
    }
}