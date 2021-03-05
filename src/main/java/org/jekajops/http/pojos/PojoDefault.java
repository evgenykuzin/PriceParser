package org.jekajops.http.pojos;

public interface PojoDefault extends Pojo {
    @Override
    default String getJsonString() {
        return gsonParse();
    }
}
