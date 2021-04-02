package com.github.evgenykuzin.core.api_integrations;

import com.github.evgenykuzin.core.api_integrations.utils.MPUtil.JsonBuilder;
import com.github.evgenykuzin.core.entities.Product;
import com.github.evgenykuzin.core.http.body_builders.BodyBuilder;
import com.github.evgenykuzin.core.http.services.HttpService;
import com.google.gson.JsonObject;

import java.util.Collection;
import java.util.List;
import java.util.Queue;

public interface MPManager <P extends Product> {
    JsonObject executeRequest(String mapping, String httpMethod, BodyBuilder bodyBuilder);

    JsonObject updateProductStocks(Queue<P> products);

    Collection<JsonObject> getProductsJsonsFromMP();

    Collection<P> getProductsFromMP();

    P constructProduct(JsonObject jsonObject);

    List<P> getOrderedProducts();

    JsonObject importProductsToMP(Collection<Product> products);

    default JsonObject executePostRequest(String mapping, JsonBuilder builder) {
        return executeRequest(mapping, HttpService.POST, builder.build());
    }

    default JsonObject executePostRequest(String mapping) {
        return executeRequest(mapping, HttpService.POST, new JsonBuilder().build());
    }
}