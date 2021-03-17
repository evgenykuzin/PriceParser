package com.github.evgenykuzin.http.body_builders;

import com.github.evgenykuzin.http.pojos.Pojo;

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
