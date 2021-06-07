package com.github.evgenykuzin.parser.analitics;

import com.github.evgenykuzin.selenium.SeleniumParser;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WordstatParser {
    private static final String wordstatUrl = "https://wordstat.yandex.ru/";

    public static void search() throws IOException {
        SeleniumParser seleniumParser = new SeleniumParser();
        seleniumParser.get(wordstatUrl);
        WebElement searchField = seleniumParser.findElement(By.className("b-form-input__input"));
        searchField.submit();
        authorize(seleniumParser);
        searchField = seleniumParser.findElement(By.className("b-form-input__input"));
        if (searchField != null) {
            searchField.sendKeys("работа");
            searchField.submit();
        }
        WebElement table = seleniumParser.findElement(By.className("b-word-statistics__table"));
        List<WebElement> rows = seleniumParser.findElements(By.tagName("tr"), table);
        Map<String, Integer> searchResult = new HashMap<>();
        for (int i = 1; i < rows.size(); i++) {
            WebElement row = rows.get(i);
            List<WebElement> rowFields = seleniumParser.findElements(By.tagName("td"), row);
            if (rowFields.size() == 2) {
                searchResult.put(rowFields.get(0).getText(), Integer.valueOf(rowFields.get(1).getText().replaceAll("&nbsp;", "")));
            }
        }
        System.out.println(searchResult);
    }

    public static void main(String[] args) throws IOException {
        search();
    }

    public static void authorize(SeleniumParser seleniumParser) {
        WebElement loginField = seleniumParser.findElement(By.id("b-domik_popup-username"));
        WebElement passField = seleniumParser.findElement(By.id("b-domik_popup-password"));
        if (loginField == null) return;
        loginField.sendKeys("eikuzin");
        passField.sendKeys("21partyholiday");
        passField.submit();
    }
}
