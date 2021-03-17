package com.github.evgenykuzin.core.util.cnfg;

public class TableConfig {
    public static class AdditionalOzonDocFieldsConfig {
        public static final String ID_COL_NAME = "id";
        public static final String LOWER_PRICE_COL_NAME = "Проценка";
        public static final String DIFF_PRICES_COL_NAME = "Разница с проценкой";
        public static final String CONCURRENT_URL_COL_NAME = "ссылка на конкурента";
        public static final String SEARCH_BARCODE_COL_NAME = "Barcode for Search";
        public static final String SUPPLIER_COL_NAME = "Поставщик";
    }

    public static class OzonDocConfig {
        public static final String OZON_PRODUCT_ID_COL_NAME = "Ozon Product ID";
        public static final String PRICE_COL_NAME = "Текущая цена с учетом скидки, руб.";
        public static final String NAME_COL_NAME = "Наименование товара";
        public static final String ARTICLE_COL_NAME = "Артикул";
        public static final String BARCODE_COL_NAME = "Barcode";
        public static final String STOCKS_COL_NAME = "Доступно на моем складе, шт";

    }

    public static class OzonUpdateConfig {
        public static final String ARTICLE_COL_NAME = "артикул";
        public static final String NAME_COL_NAME = "имя (необязательно)";
        public static final String STOCKS_COL_NAME = "количество";
        public static final String PRICE_COL_NAME = "";
    }

    public static class XmarketConfig {
        public static final String ID_COL_NAME = "ELEMENT_ID";
        public static final String PRICE_COL_NAME = "Цена клиента";
        public static final String NAME_COL_NAME = "Название";
        public static final String ARTICLE_COL_NAME = "Артикул";
        public static final String BARCODE_COL_NAME = "Штрихкод";
        public static final String STOCKS_COL_NAME = "Остаток на складе";
    }

    public static class SexTrgConfig {
        public static final String ID_COL_NAME = "C1";
        public static final String NAME_COL_NAME = "C3";
        public static final String ARTICLE_COL_NAME = "C2";
        public static final String BARCODE_COL_NAME = "C15";
        public static final String STOCKS_COL_NAME = "C14";
    }

    public static class SuppliersNamesConfig {
        public static final String SexTrgSupplierConst = "SexOptTorg";
        public static final String XmarketSupplierConst = "Xmarket";
    }

}
