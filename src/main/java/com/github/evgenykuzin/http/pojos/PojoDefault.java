package com.github.evgenykuzin.http.pojos;

public interface PojoDefault extends Pojo {
    @Override
    default String getJsonString() {
        return gsonParse();
    }
}
