package org.jekajops.http.body_builders;

import org.jekajops.http.pojos.Pojo;

public class PojoBuilder implements BodyBuilder{
    Pojo pojo;

    public PojoBuilder(Pojo pojo) {
        this.pojo = pojo;
    }

    @Override
    public String getJsonString() {
        return pojo.getJsonString();
    }

}
