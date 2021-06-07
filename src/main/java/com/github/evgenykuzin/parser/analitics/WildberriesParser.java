package com.github.evgenykuzin.parser.analitics;

import com.github.evgenykuzin.core.entities.product.Product;
import com.google.common.base.CharMatcher;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class WildberriesParser {
    private static final String WB_MAIN_LINK = "https://www.wildberries.ru";
    private static final String WB_SEARCH_LINK = WB_MAIN_LINK + "/catalog/0/search.aspx?";

    public static Elements parseCategory(String link) {
        Document doc;
        try {
            doc = Jsoup.parse(new URL(link), 0);
            return doc.getElementsByClass("dtList-inner");
        } catch (HttpStatusException hse) {
            hse.printStackTrace();
            System.out.println("error parsing url: " + hse.getUrl());
            if (link.contains(".ru")) {
                System.out.println("trying '.kz' ...");
                return parseCategory(link.replace(".ru", ".kz"));
            } else {
                System.out.println("failed to parse url with '.kz'");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new Elements();
    }

    public static Product parseProduct(Element element, Set<String> ignoredBrands) {
        Product product;
        String productName = element.getElementsByClass("goods-name c-text-sm").text();
        String brandName = element.getElementsByClass("brand-name c-text-sm").text();
        if (ignoredBrands.contains(brandName)) return null;
        String np = element.getElementsByClass("lower-price").text();
        String dp = element.getElementsByClass("price-sale active").text();
        String op = element.getElementsByClass("price-old-block").html().split("</del>")[0];
        double newPrice = getDouble(np);
        double discountPercent = getDouble(dp);
        double oldPrice = getDouble(op);
        if (newPrice == -1 || oldPrice == -1 || discountPercent == -1) return null;
        String url = element.getElementsByClass("ref_goods_n_p j-open-full-product-card").attr("href");
        if (!url.split("")[0].equals("/")) url = "/" + url;
        if (!url.contains(WB_MAIN_LINK)) url = WB_MAIN_LINK + url;
//        String seller = null;
//        try {
//            System.out.println(url);
//            Document document = Jsoup.parse(new URL(url), 0);
//            System.out.println(document.getElementsByClass("seller").first());
//            seller = document.getElementsByClass("seller__text").stream()
//                    .map(Element::text)
//                    .collect(Collectors.joining());
//            System.out.println(seller);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        product = new Product();
        product.setName(productName);
        product.setBrandName(brandName);
//        product.setArticle(seller);
        //product.set
//        product.set(newPrice);
//        product.setOldPrice(oldPrice);
//        product.setDiscountPercent(discountPercent);
        return product;
    }

    public static List<Product> parseProducts(String link, Set<String> ignoredBrands) {
        System.out.println(link);
        return parseCategory(link).stream()
                .map(element -> parseProduct(element, ignoredBrands))
                .collect(Collectors.toList());
    }

    public static List<Product> search(String key) {
        List<Product> products;
        int page = 1;
        List<Product> result = new ArrayList<>();
        List<Product> last = new ArrayList<>();
        while (!(products = search(key, page++)).isEmpty()) {
            if (last.containsAll(products)) break;
            result.addAll(products);
            last = new ArrayList<>(products);
        }
        return result;
    }

    public static List<Product> search(String key, int page) {
        String paramString = String.format("search=%s&xsearch=true&page=%d", URLEncoder.encode(key, StandardCharsets.UTF_8), page);
        return parseProducts(
                WB_SEARCH_LINK + paramString,
                Set.of());
    }

    public static Integer searchAndCountSellers(String key) {
        return search(key)
                .stream()
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(Product::getBrandName))
                .size();
    }

    private static double getDouble(String string) {
        try {
            return Double.parseDouble(CharMatcher.digit().retainFrom(string));
        } catch (NumberFormatException ignored) {
            return -1;
        }
    }

    public static void main(String[] args) {
        System.out.println(searchAndCountSellers("поводок"));
    }
}
