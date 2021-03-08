package org.jekajops.http.pojos;

import com.google.gson.Gson;

public interface Pojo {
    String getJsonString();
    default String gsonParse() {
        return new Gson().toJson(this);
    }
}
