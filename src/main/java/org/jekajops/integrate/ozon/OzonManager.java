package org.jekajops.integrate.ozon;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.AllArgsConstructor;
import org.jekajops.entities.OzonProduct;
import org.jekajops.entities.Product;
import org.jekajops.http.body_builders.Body;
import org.jekajops.http.body_builders.BodyBuilder;
import org.jekajops.http.body_builders.JsonBodyBuilder;
import org.jekajops.http.headers.Header;
import org.jekajops.http.headers.HeadersModel;
import org.jekajops.http.headers.HeadersModelImpl;
import org.jekajops.http.models.ResponseModel;
import org.jekajops.http.services.ClosableHttpService;
import org.jekajops.http.services.HttpService;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class OzonManager {
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
                            null
                    );
                    set.add(ozonProduct);
                }
            }
        }
        return set;
    }

    public void updateProductStocks(OzonProduct ozonProduct, String warehouseId, int stock) throws IOException {
        var j = new JsonBuilder()
                .addNewArr("stocks")
                .addInArr(new JsonBuilder()
                        .addProperty("offer_id", ozonProduct.getArticle())
                        .addProperty("product_id", ozonProduct.getOzonProductId())
                        .addProperty("stock", String.valueOf(stock))
                        .addProperty("warehouse_id", warehouseId), "stocks");

        System.out.println("j = " + j);
        var resInf = executePostRequest("/v2/products/stocks", j);
        System.out.println("res /v2/products/stocks = " + resInf);
    }

    public static void main(String[] args) {
        var m = new OzonManager();
        var v = m.getActualPricesProducts();
    }

    class JsonBuilder {
        private JsonObject jsonObject;

        public JsonBuilder() {
            this.jsonObject = new JsonObject();
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
