package com.github.evgenykuzin.core.api_integrations.ozon;

import com.github.evgenykuzin.core.entities.OzonProduct;
import com.github.evgenykuzin.core.entities.Product;
import com.github.evgenykuzin.core.entities.Table;
import com.github.evgenykuzin.core.http.body_builders.BodyBuilder;
import com.github.evgenykuzin.core.http.body_builders.JsonBodyBuilder;
import com.github.evgenykuzin.core.http.headers.Header;
import com.github.evgenykuzin.core.http.headers.HeadersModel;
import com.github.evgenykuzin.core.http.headers.HeadersModelImpl;
import com.github.evgenykuzin.core.http.services.ClosableHttpService;
import com.github.evgenykuzin.core.http.services.HttpService;
import com.github.evgenykuzin.core.util.loger.Loggable;
import com.google.gson.*;
import lombok.AllArgsConstructor;

import java.io.IOException;
import java.util.*;

public class OzonManager implements Loggable {
    private static final String OZON_CLIENT_ID = "123973";
    private static final String OZON_API_KEY = "9ad71481-0513-4bd2-9b95-5845db520dec";
    private static final String OZON_API_HOST = "http://api-seller.ozon.ru";

    private final ClosableHttpService httpService;

    public OzonManager() {
        this.httpService = new ClosableHttpService();
    }

    public JsonObject executeRequest(String mapping, String httpMethod, BodyBuilder bodyBuilder) throws IOException {
        HeadersModel headers = new HeadersModelImpl(
                new Header("Client-Id", OZON_CLIENT_ID),
                new Header("Api-Key", OZON_API_KEY),
                new Header("Content-Type", "application/json")
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

    public JsonObject executePostRequest(String mapping, JsonBuilder builder) throws IOException {
        return executeRequest(mapping, HttpService.POST, builder.build());
    }

    public JsonObject executePostRequest(String mapping) throws IOException {
        return executeRequest(mapping, HttpService.POST, new JsonBuilder().build());
    }


    public Set<Product> getActualPricesProducts() {
        var manager = new OzonManager();
        var set = new HashSet<Product>();
        for (int i = 0; i < 15; i++) {
            String iStr = String.valueOf(i);
            JsonObject res = new JsonObject();
            try {
                res = manager.executePostRequest("/v1/product/info/prices",
                        new JsonBuilder()
                        .addProperty("page", iStr)
                        .addProperty("page_size", "555")
                );
            } catch (IOException e) {
                e.printStackTrace();
            }
            var jsonArray = res.getAsJsonObject("result").getAsJsonArray("items");
            if (jsonArray.size() < 1) break;
            for (int j = 0; j < jsonArray.size(); j++) {
                JsonObject je = jsonArray.get(j).getAsJsonObject();
                var price = je.getAsJsonObject("price").get("price").getAsString();
                if (price != null && !price.isEmpty()) {
                    var ozonProduct = new OzonProduct(
                            1,
                            Double.parseDouble(price),
                            null,
                            null,
                            je.get("offer_id").getAsString(),
                            null,
                            je.get("product_id").getAsString(),
                            null,
                            null,
                            null
                    );
                    set.add(ozonProduct);
                }
            }
        }
        return set;
    }

    public JsonObject updateProductStocks(Queue<OzonProduct> ozonProducts) throws IOException {
        var jsonBuilder = new JsonBuilder()
                .addNewArr("stocks");
        while (!ozonProducts.isEmpty()) {
            var ozonProduct = ozonProducts.poll();
            var stock = ozonProduct.getStock();
            jsonBuilder.addInArr(new JsonBuilder()
                            .addProperty("offer_id", ozonProduct.getArticle())
                            .addProperty("product_id", ozonProduct.getOzonProductId())
                            .addProperty("stock", String.valueOf(stock))
                    , "stocks");
        }
        System.out.println("jsonBuilder = " + jsonBuilder);
        return executePostRequest("/v1/products/stocks", jsonBuilder);
    }

    public Collection<Map<String, String>> getProductsFromOzon() throws IOException {
        var arrayList = new ArrayList<Map<String, String>>();
        var req = executePostRequest("/v1/product/list");
        System.out.println("req = " + req);
        var jarr = req.getAsJsonObject("result")
                .getAsJsonArray("items");
        for (JsonElement element : jarr) {
            var article = element.getAsJsonObject()
                    .get("offer_id")
                    .getAsString();
            var jsonBuilder = new JsonBuilder()
                    .addProperty("offer_id", article);
            var productJson = executePostRequest("/v2/product/info", jsonBuilder);
            System.out.println("productJson = " + productJson);
        }
        return arrayList;
    }

    public JsonObject importProductsToOzon(Table table) throws IOException {
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

    public static void main(String[] args) throws IOException {
        var x = new OzonManager();
        var j = new JsonBuilder();
        var r = x.executePostRequest("/v1/category/tree");
        //System.out.println("r = " + r);
        j = new JsonBuilder("{\n" +
                "  \"attribute_type\": \"required\",\n" +
                "  \"category_id\": 17027484,\n" +
                "  \"language\": \"RU\"\n" +
                "}");
        r = x.executePostRequest("/v2/category/attribute", j);
        System.out.println("r2 = " + r);
        //x.importProductsToOzon(null);
    }

    @AllArgsConstructor
    static class JsonBuilder {
        private final JsonObject jsonObject;

        public JsonBuilder() {
            this.jsonObject = new JsonObject();
        }

        public JsonBuilder(String string) {
            this.jsonObject = new JsonParser().parse(string).getAsJsonObject();
        }

        public JsonBuilder addProperty(String k, String v) {
            jsonObject.addProperty(k, v);
            return this;
        }

        public JsonBuilder addPropertyInObj(String k, String v, String in) {
            jsonObject.get(in).getAsJsonObject().addProperty(k, v);
            return this;
        }

        public JsonBuilder addPropertyInArr(String v, String in) {
            jsonObject.get(in).getAsJsonArray().add(v);
            return this;
        }

        public JsonBuilder addInArr(JsonBuilder jsonBuilder, String in) {
            jsonObject.get(in).getAsJsonArray().add(jsonBuilder.build().getJsonObject());
            return this;
        }

        public JsonBuilder add(String k, JsonElement v) {
            jsonObject.add(k, v);
            return this;
        }

        public JsonBuilder addNew(String k) {
            return add(k, new JsonObject());
        }

        public JsonBuilder addNewArr(String k) {
            return add(k, new JsonArray());
        }

        public BodyBuilder build() {
            return (JsonBodyBuilder) object -> new JsonBodyBuilder.BodyJson(jsonObject);
        }

        @Override
        public String toString() {
            return jsonObject.toString();
        }
    }
}
