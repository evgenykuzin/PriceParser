package org.jekajops.parser.shop;

import org.jekajops.entities.OzonProduct;
import org.jekajops.entities.Product;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class OzonParser implements ShopParser {
    private static final String URL = "https://www.ozon.ru/search/?from_global=true&page=%d&text=%s";
    private static final String URL2 = "https://www.ozon.com/search/";

    private static final String PRODUCTS_CLASS = "a0c6 a0c9 a0c8";

    @Override
    public List<Product> parseProducts(String key) {
        var products = new ArrayList<Product>();
        try {
            int page = 1;
            Elements elements;
            while (!(elements = getProductsElements(page, key)).isEmpty()) {
                if (page > 15) break;
                for (Element element : elements) {
                    var name = element.getElementsByClass("a2g0 tile-hover-target").get(0).text();
                    System.out.println("name = " + name);
                    var price = Double.parseDouble(element.getElementsByClass("a0y9 tile-hover-target").get(0).getElementsByTag("span").text().replaceAll("\\D", ""));
                    System.out.println("price = " + price);
                    products.add(new OzonProduct(0, price, name, null, null));
                }
                page++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return products;
    }

    @Override
    public void quit() {

    }

    private static Document getDocument(int page, String key) throws IOException {
        var params = new HashMap<String, String>();
        params.put("from_global", "true");
        params.put("page", String.valueOf(page));
        params.put("text", key);
        System.out.println("params = " + params);
        Proxy proxy = new Proxy(
                Proxy.Type.HTTP,
                InetSocketAddress.createUnresolved("146.66.172.217", 8080)
        );
        return Jsoup.connect(URL2)
                .data(params)
                //.proxy(proxy)
                .method(Connection.Method.GET)
                .followRedirects(true)
                .timeout(10000)
                .userAgent("Mozilla")
                .get();
    }

    private static Elements getProductsElements(int page, String key) throws IOException {
        return getDocument(page, key).getElementsByClass(PRODUCTS_CLASS);
    }

    private static String getUrl(int page, String key) {
        System.out.println("page = " + String.format(URL, page, key));
        return String.format(URL, page, key);
    }
}
