package org.jekajops.http.services;

import org.jekajops.http.body_builders.BodyBuilder;
import org.jekajops.http.headers.HeadersModel;
import org.jekajops.http.headers.HeadersModelImpl;
import org.jekajops.http.models.ClientModel;
import org.jekajops.http.models.RequestModel;
import org.jekajops.http.models.ResponseModel;

import java.util.Map;

public interface HttpService<R, C> {
    String POST = "POST";
    String GET = "GET";
    String PUT = "PUT";
    String DELETE = "DELETE";
    String TYPE_JSON = "application/json; charset=UTF-8";
    ClientModel<C> constructClient();
    RequestModel<R> constructRequest(String url, String method, HeadersModel headers, BodyBuilder bodyBuilder);
    ResponseModel getResponse(RequestModel<R> request);

    default RequestModel<R> constructGetRequest(String url, String... params) {
        if (params.length > 0) url += "?";
        StringBuilder urlBuilder = new StringBuilder(url);
        for (int i = 0; i < params.length; i++) {
            urlBuilder.append(params[i]);
            if (i != params.length - 1) urlBuilder.append("&");
        }
        return constructRequest(urlBuilder.toString(), GET, new HeadersModelImpl(), BodyBuilder.NO_BODY);
    }

    default RequestModel<R> constructGetRequest(String url, Map<String, String> params) {
        if (params != null && !params.isEmpty()) {
            url += "?";
            StringBuilder urlBuilder = new StringBuilder(url);
            params.forEach((k, v) -> urlBuilder
                    .append(k)
                    .append("=")
                    .append(v)
                    .append("&"));
            urlBuilder.deleteCharAt(urlBuilder.lastIndexOf("&"));
            url = urlBuilder.toString();
        }
        return constructRequest(url, GET, new HeadersModelImpl(), BodyBuilder.NO_BODY);
    }
}
