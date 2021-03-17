package com.github.evgenykuzin.integrate.ozon;

import com.github.evgenykuzin.entities.OzonProduct;
import com.github.evgenykuzin.http.body_builders.BodyBuilder;
import com.github.evgenykuzin.http.headers.HeadersModel;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.github.evgenykuzin.app.loger.Loggable;
import com.github.evgenykuzin.entities.Product;
import com.github.evgenykuzin.http.body_builders.JsonBodyBuilder;
import com.github.evgenykuzin.http.headers.Header;
import com.github.evgenykuzin.http.headers.HeadersModelImpl;
import com.github.evgenykuzin.http.services.ClosableHttpService;
import com.github.evgenykuzin.http.services.HttpService;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;

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
                            null
                    );
                    set.add(ozonProduct);
                }
            }
        }
        return set;
    }

    public void updateProductStocks(Collection<OzonProduct> ozonProducts) throws IOException {
        var jsonBuilder = new JsonBuilder()
                .addNewArr("stocks");
        var mainQueue = new ArrayBlockingQueue<OzonProduct>(ozonProducts.size());
        mainQueue.addAll(ozonProducts);
        while (!mainQueue.isEmpty()) {
            var queue = new ArrayBlockingQueue<OzonProduct>(500);
            while (queue.remainingCapacity() > 0) {
                try {
                    queue.put(mainQueue.poll());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            while (!queue.isEmpty()) {
                var ozonProduct = queue.poll();
                var stock = ozonProduct.getStock() > 0 ? ozonProduct.getStock()-1 : 0;
                jsonBuilder.addInArr(new JsonBuilder()
                                .addProperty("offer_id", ozonProduct.getArticle())
                                .addProperty("product_id", ozonProduct.getOzonProductId())
                                .addProperty("stock", String.valueOf(stock))
                        , "stocks");
            }
            System.out.println("jsonBuilder = " + jsonBuilder);
            var resInf = executePostRequest("/v1/products/stocks", jsonBuilder);
            System.out.println("resInf = " + resInf);
            if (resInf.getAsJsonArray("errors").size() != 0){
                log(resInf.toString());
            }
        }

    }

    static class JsonBuilder {
        private final JsonObject jsonObject;

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
