package com.github.evgenykuzin.core.marketplace_api_integrations.wildeberries;

import com.github.evgenykuzin.core.db.dao.ProductDAO;
import com.github.evgenykuzin.core.entities.product.Product;
import com.github.evgenykuzin.core.entities.product.WildeberriesProduct;
import com.github.evgenykuzin.core.marketplace_api_integrations.MPManager;
import com.github.evgenykuzin.core.marketplace_api_integrations.MP_NAME;
import com.github.evgenykuzin.core.marketplace_api_integrations.utils.MPUtil;
import com.github.evgenykuzin.core.util.http.body_builders.BodyBuilder;
import com.github.evgenykuzin.core.util.http.headers.HeadersModelImpl;
import com.github.evgenykuzin.core.util.http.services.ClosableHttpService;
import com.github.evgenykuzin.core.util_managers.data_managers.XlsxDataManager;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.File;
import java.util.Collection;
import java.util.List;

public class WildeberriesManager implements MPManager<Product> {
    private static final String TOKEN = "6ee3655d4e11ce5af54d0340308f7225b7343fbd37c3efe04a0ef938a712d449";
    private static final Integer storeId = 4345;

    @Override
    public JsonObject executeRequest(String mapping, String httpMethod, BodyBuilder bodyBuilder) {
        var service = new ClosableHttpService();

        var req = service.constructRequest(
                "https://wbxgate.wildberries.ru/" + mapping,
                httpMethod,
                new HeadersModelImpl(),
                bodyBuilder
        );
        var responseString = service.getResponse(req).getResponseString();
        var result = new Gson()
                .fromJson(responseString, JsonObject.class);
        System.out.println("result = " + result);
        return result;
    }

    @Override
    public JsonObject updateProductStocks(Collection<Product> products, String supplierName) {
        return executeWithMaxItems(products, 500, productsForRequest -> {
            var json = new MPUtil.JsonBuilder()
                    .addProperty("token", TOKEN)
                    .addNewArr("data");
            for (var product : productsForRequest) {
                WildeberriesProduct wildeberriesProduct = product.getWildeberriesProduct();
                if (wildeberriesProduct == null) continue;
                Integer wbId = wildeberriesProduct.getWbId();
                json.addInArr(new MPUtil.JsonBuilder()
                                .addIntProperty("nmId", wbId)
                                .addNewArr("stocks")
                                .addInArr(new MPUtil.JsonBuilder()
                                        .addIntProperty("chrtId", Integer.valueOf(wildeberriesProduct.getChrtId()))
                                        .addIntProperty("price", wildeberriesProduct.getPrice().intValue())
                                        .addIntProperty("quantity", product.getStock().computeStock())
                                        .addIntProperty("storeId", storeId), "stocks")
                        , "data");
            }
            System.out.println("json = " + json);

            return executePostRequest("stocks", json);
        });
    }

    @Override
    public Collection<JsonObject> getProductsJsonsFromMP() {
        return null;
    }

    @Override
    public Product constructProduct(JsonObject jsonObject) {
        return null;
    }

    @Override
    public JsonObject constructJsonFromProduct(Product product) {
        return null;
    }

    @Override
    public List<Product> getOrderedProducts() {
        return null;
    }

    @Override
    public JsonObject importProductsToMP(Collection<Product> products) {
        return null;
    }

    @Override
    public MP_NAME getName() {
        return MP_NAME.WILDEBERRIES;
    }

    public static WildeberriesManager getInstance() {
        return WildeberriesManagerHolder.WILDEBERRIES_MANAGER;
    }

    private static class WildeberriesManagerHolder {
        public static final WildeberriesManager WILDEBERRIES_MANAGER = new WildeberriesManager();
    }

    public static void main(String[] args) {
        File file = new File("C:\\Users\\JekaJops\\Downloads\\report_2021_4_26.XLSX");
        ProductDAO productDAO = ProductDAO.getInstance();
        XlsxDataManager
                .getDefaultXslsDataManager(file, "Артикул WB")
                .parseTable().forEach((s, row) -> {
            String article = parseField(row.get("Артикул ИМТ"));
            String brandName = row.get("Бренд");
            List<Product> products = productDAO.searchByArticleAndBrand(article, brandName);
            if (products != null && !products.isEmpty()) {
                if (products.size() > 1) System.out.println("products = " + products);
                Product product = products.get(0);
                WildeberriesProduct wildeberriesProduct = new WildeberriesProduct();
                var wbIdStr = parseField(row.get("Артикул WB"));
                wildeberriesProduct.setWbId(Integer.valueOf(wbIdStr));
                wildeberriesProduct.setCategory(row.get("Предмет"));
                wildeberriesProduct.setChrtId(parseField(row.get("Код размера (chrt_id)")));
                wildeberriesProduct.setProductId(product.getId());
                product.setWildeberriesProduct(wildeberriesProduct);
                productDAO.update(product);
                System.out.println("product = " + product);
            }
        });
    }

    public static String parseField(String field) {
        if (field.contains(".") && field.contains("E7")) {
            field = field
                    .replaceAll("\\.", "")
                    .replaceAll("E7", "");
        }
        return field;
    }
}
