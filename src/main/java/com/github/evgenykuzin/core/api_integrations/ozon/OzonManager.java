package com.github.evgenykuzin.core.api_integrations.ozon;

import com.github.evgenykuzin.core.api_integrations.MPManager;
import com.github.evgenykuzin.core.entities.OzonProduct;
import com.github.evgenykuzin.core.entities.Product;
import com.github.evgenykuzin.core.http.body_builders.BodyBuilder;
import com.github.evgenykuzin.core.http.headers.Header;
import com.github.evgenykuzin.core.http.headers.HeadersModel;
import com.github.evgenykuzin.core.http.headers.HeadersModelImpl;
import com.github.evgenykuzin.core.http.services.ClosableHttpService;
import com.github.evgenykuzin.core.http.services.HttpService;
import com.github.evgenykuzin.core.util.loger.Loggable;
import com.github.evgenykuzin.core.util_managers.PropertiesManager;
import com.google.gson.*;

import java.util.*;
import java.util.stream.Collectors;

import static com.github.evgenykuzin.core.api_integrations.ozon.OzonManagerUtil.getSku;
import static com.github.evgenykuzin.core.api_integrations.utils.MPUtil.*;

public class OzonManager implements MPManager<OzonProduct>, Loggable {
    private static final Properties ozonProps = PropertiesManager.getProperties("ozon");
    private static final String OZON_CLIENT_ID = ozonProps.getProperty("client-id");
    private static final String OZON_API_KEY = ozonProps.getProperty("api-key");
    private static final String OZON_API_HOST = "http://api-seller.ozon.ru";

    private final ClosableHttpService httpService;

    public OzonManager() {
        this.httpService = new ClosableHttpService();
    }

    public JsonObject executeRequest(String mapping, String httpMethod, BodyBuilder bodyBuilder) {
        HeadersModel headers = new HeadersModelImpl(
                new Header("Client-Id", OZON_CLIENT_ID),
                new Header("Api-Key", OZON_API_KEY),
                new Header("Content-Type", "application/json; charset=utf-8"),
                new Header("Accept-Charset", "ISO-8859-1,utf-8;q=0.7,*;q=0.7")
        );
        var req = httpService.constructRequest(
                OZON_API_HOST + mapping,
                httpMethod,
                headers,
                bodyBuilder
        );
        return new Gson()
                .fromJson(httpService
                        .getResponse(req)
                        .getResponseString(), JsonObject.class);
    }

    public JsonObject executePostRequest(String mapping, JsonBuilder builder) {
        return executeRequest(mapping, HttpService.POST, builder.build());
    }

    public JsonObject executePostRequest(String mapping) {
        return executeRequest(mapping, HttpService.POST, new JsonBuilder().build());
    }

    @Override
    public JsonObject updateProductStocks(Queue<OzonProduct> ozonProducts) {
        var jsonBuilder = new JsonBuilder()
                .addNewArr("stocks");
        while (!ozonProducts.isEmpty()) {
            var ozonProduct = ozonProducts.poll();
            var stock = ozonProduct.getStock();
            jsonBuilder.addInArr(new JsonBuilder()
                            .addProperty("product_id", String.valueOf(ozonProduct.getId()))
                            .addProperty("stock", String.valueOf(stock))
                    , "stocks");
        }
        log("jsonBuilder = " + jsonBuilder);
        return executePostRequest("/v1/products/stocks", jsonBuilder);
    }

    @Override
    public Collection<JsonObject> getProductsJsonsFromMP() {
        var resultCollection = new ArrayList<JsonObject>();
        JsonArray productsListJson;
        int page = 0;
        do {
            page++;
            var pageFilter = new JsonBuilder()
                    .addProperty("page", String.valueOf(page))
                    .addProperty("page_size", "50");
            var req = executePostRequest("/v1/product/list", pageFilter);
            productsListJson = req.getAsJsonObject("result")
                    .getAsJsonArray("items");
            JsonBuilder infoJsonBuilder = new JsonBuilder()
                    .addNewArr("product_id");
            for (JsonElement element : productsListJson) {
                var article = element.getAsJsonObject()
                        .get("product_id")
                        .getAsString();
                infoJsonBuilder.addPropertyInArr(article, "product_id");
            }
            var infoResult = executePostRequest("/v2/product/info/list", infoJsonBuilder);
            var resultJson = infoResult.get("result");
            if (resultJson != null) {
                var infoJsonArray = infoResult.get("result")
                        .getAsJsonObject()
                        .getAsJsonArray("items");
                for (JsonElement element : infoJsonArray) {
                    resultCollection.add(element.getAsJsonObject());
                }
            }
        } while (productsListJson.size() > 0);
        return resultCollection;
    }

    @Override
    public Collection<OzonProduct> getProductsFromMP() {
        return getProductsJsonsFromMP().stream()
                .map(this::constructProduct)
                .collect(Collectors.toList());
    }

    @Override
    public OzonProduct constructProduct(JsonObject jsonObject) {
        Long id = getLongProp(jsonObject, "id");
        String article = getStrProp(jsonObject, "offer_id");
        String barcode = getStrProp(jsonObject, "barcode");
        String name = getStrProp(jsonObject, "name");
        Double price = getDoubleProp(jsonObject, "price");
        JsonObject stocksObj = getJsonProp(jsonObject, "stocks");
        Integer stock = 0;
        if (stocksObj != null) {
            stock = getIntProp(stocksObj, "present");
        }
        if (stock == null) stock = 0;
        String skuFbs = getSku(jsonObject, "fbs");
        String skuFbo = getSku(jsonObject, "fbo");
        Long categoryId = getLongProp(jsonObject, "category_id");
        String brand = null;
        Double concPrice = null;
        String concProdUrl = null;
        String searchBarcode = null;
        String supplier = null;
        return new OzonProduct(
                id,
                null,
                name,
                article,
                barcode,
                searchBarcode,
                price,
                skuFbs,
                skuFbo,
                stock,
                brand,
                supplier,
                categoryId,
                concPrice,
                concProdUrl
        );
    }

    private Map<String, String> constructProductMap(JsonObject jsonObject) {
        Map<String, String> map = new HashMap<>();
        var article = getStrProp(jsonObject, "offer_id");

        return map;
    }

    public OzonProduct getProductFromOzonBy(String by, String key) {
        var resp = executePostRequest("/v2/product/info", new JsonBuilder().addProperty(by, key));
        return constructProduct(resp.getAsJsonObject("result"));
    }

    @Override
    public List<OzonProduct> getOrderedProducts() {
        var result = new ArrayList<OzonProduct>();
        var resp = executePostRequest("/v2/posting/fbs/list", new JsonBuilder("{\n" +
                "  \"dir\": \"asc\",\n" +
                "  \"filter\": {\n" +
                "    \"status\": \"awaiting_packaging\"\n" +
                "  },\n" +
                "  \"limit\": 50,\n" +
                "  \"offset\": 0,\n" +
                "  \"with\": {\n" +
                "    \"barcodes\": true\n" +
                "  }\n" +
                "}"));
        JsonArray productsJson;
        JsonArray resultArray = resp.getAsJsonArray("result");
        if (resultArray == null || resultArray.size() <= 0) return new ArrayList<>();
        productsJson = resultArray.get(0)//!!!!!!!!!!!!!!!!!!!!!ATTENTION !!!!!!!!!!!!!!!!!!
                .getAsJsonObject()
                .getAsJsonArray("products");
        for (var element : productsJson) {
            var jsonProduct = element.getAsJsonObject();
            var sku = jsonProduct.get("sku").getAsString();
            var name = jsonProduct.get("name").getAsString();
            var quantity = jsonProduct.get("quantity").getAsString();
            var article = jsonProduct.get("offer_id").getAsString();
            var product = getProductFromOzonBy("sku", sku);
            result.add(product);
        }
        return result;
    }

    public JsonObject updateOldAndPremiumPriceForProducts(Collection<OzonProduct> products) {
        JsonBuilder jsonBuilder = new JsonBuilder();
        jsonBuilder.addNewArr("prices");
        products.forEach(product -> {
            double price = product.getPrice();
            double oldPrice = price + ((price / 100) * (25 + new Random().nextInt(25)));
            double premiumPrice = price - ((price / 100) * 7);
            JsonBuilder priceBody = new JsonBuilder()
                    .addProperty("product_id", String.valueOf(product.getId()))
                    .addProperty("price", String.valueOf(price))
                    .addProperty("old_price", String.valueOf(oldPrice))
                    .addProperty("premium_price", String.valueOf(premiumPrice));
            jsonBuilder.addInArr(priceBody, "prices");
        });
        return executePostRequest("/v1/product/import/prices", jsonBuilder);
    }

    public JsonObject importProductsToMP(Collection<Product> products) {
        var jsn = new JsonBuilder(new JsonParser().parse("{\n" +
                "  \"filter\": {\n" +
                "    \"product_id\": [\n" +
                "      53571758\n" +
                "    ]\n" +
                "  }\n" +
                "}").getAsJsonObject());
        var req = executePostRequest("/v2/products/info/attributes", jsn);
        System.out.println("req = " + req);
        return req;
    }

    public static void main(String[] args) {
        var x = new OzonManager();
        var r = x.executePostRequest("/v1/category/tree");
        System.out.println("r = " + r);
    }


}
