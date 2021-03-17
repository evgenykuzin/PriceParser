package com.github.evgenykuzin.http.services;

import com.github.evgenykuzin.http.body_builders.BodyBuilder;
import com.github.evgenykuzin.http.body_builders.JsonBodyBuilder;
import com.github.evgenykuzin.http.headers.HeadersModel;
import com.github.evgenykuzin.http.models.ClientModel;
import com.github.evgenykuzin.http.models.RequestModel;
import com.github.evgenykuzin.http.models.RequestModelDefault;
import com.github.evgenykuzin.http.models.ResponseModel;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

public class NetHttpService implements HttpService<HttpRequest, HttpClient> {
    @Override
    public ClientModel<HttpClient> constructClient() {
        return HttpClient::newHttpClient;
    }

    @Override
    public RequestModel<HttpRequest> constructRequest(String url, String method, HeadersModel headers, BodyBuilder bodyBuilder) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url));
        String[] headersArray = headers.getSet().stream().map(header -> header.getKey()+" : "+header.getValue()).toArray(String[]::new);
        if (headersArray.length > 0) {
            builder.headers(headersArray);
        }
        HttpRequest request = builder
                .method(method, HttpRequest.BodyPublishers.ofString(bodyBuilder.getJsonString()))
                .build();
        return new RequestModelDefault<>(
                url,
                request.toString(),
                bodyBuilder.getJsonString(),
                request
        );
    }

    public RequestModel<HttpRequest> constructRequest(String url, String method, HeadersModel headersModel, Map<String, String> params) {
        return constructRequest(url, method, headersModel, (JsonBodyBuilder) (object) -> () -> {
            params.forEach(object::addProperty);
            return object;
        });
    }

    @Override
    public ResponseModel getResponse(RequestModel<HttpRequest> request) {
        try {
            HttpClient client = constructClient().client();
            HttpResponse<String> response = client.send(request.getRequest(),
                    HttpResponse.BodyHandlers.ofString());
            return new ResponseModel(
                    response.body(),
                    "",
                    response.statusCode()
            );
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return ResponseModel.EMPTY;
        }
    }

}
