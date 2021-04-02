package com.github.evgenykuzin.core.api_integrations.yandex_market;

import com.github.evgenykuzin.core.api_integrations.MPManager;
import com.github.evgenykuzin.core.entities.Product;
import com.github.evgenykuzin.core.entities.YandexMarketProduct;
import com.github.evgenykuzin.core.http.body_builders.BodyBuilder;
import com.github.evgenykuzin.core.http.headers.Header;
import com.github.evgenykuzin.core.http.headers.HeadersModel;
import com.github.evgenykuzin.core.http.headers.HeadersModelImpl;
import com.github.evgenykuzin.core.http.services.ClosableHttpService;
import com.github.evgenykuzin.core.util.loger.Loggable;
import com.github.evgenykuzin.core.util_managers.PropertiesManager;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.Queue;

public class YandexMarketManager implements MPManager<YandexMarketProduct>, Loggable {
    private static final Properties yandexMarketProps = PropertiesManager.getProperties("yandex-market");
    private static final String YM_CAMPAIGN_ID = yandexMarketProps.getProperty("campaign-id");
    private static final String YM_API_KEY = yandexMarketProps.getProperty("api-key");
    private static final String YM_API_HOST = String.format("https://api.partner.market.yandex.ru/v2/campaigns/%s/", YM_CAMPAIGN_ID);

    private final ClosableHttpService httpService;

    public YandexMarketManager() {
        this.httpService = new ClosableHttpService();
    }

    @Override
    public JsonObject executeRequest(String mapping, String httpMethod, BodyBuilder bodyBuilder) {
        HeadersModel headers = new HeadersModelImpl(
                new Header("Api-Key", YM_API_KEY),
                new Header("Content-Type", "application/json; charset=utf-8"),
                new Header("Accept-Charset", "ISO-8859-1,utf-8;q=0.7,*;q=0.7")
        );
        var req = httpService.constructRequest(
                YM_API_HOST + mapping,
                httpMethod,
                headers,
                bodyBuilder
        );
        return new Gson()
                .fromJson(httpService
                        .getResponse(req)
                        .getResponseString(), JsonObject.class);
    }

    @Override
    public JsonObject updateProductStocks(Queue<YandexMarketProduct> products) {
        return null;
    }

    @Override
    public Collection<JsonObject> getProductsJsonsFromMP() {
        return null;
    }

    @Override
    public Collection<YandexMarketProduct> getProductsFromMP() {
        return null;
    }

    @Override
    public YandexMarketProduct constructProduct(JsonObject jsonObject) {
        return null;
    }

    @Override
    public List<YandexMarketProduct> getOrderedProducts() {
        return null;
    }

    @Override
    public JsonObject importProductsToMP(Collection<Product> products) {
        return null;
    }
}
