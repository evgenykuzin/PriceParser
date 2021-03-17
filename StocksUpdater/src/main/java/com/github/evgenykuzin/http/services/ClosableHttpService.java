package com.github.evgenykuzin.http.services;

import com.github.evgenykuzin.http.body_builders.BodyBuilder;
import com.github.evgenykuzin.http.body_builders.JsonBodyBuilder;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import com.github.evgenykuzin.http.headers.Header;
import com.github.evgenykuzin.http.headers.HeadersModel;
import com.github.evgenykuzin.http.headers.HeadersModelImpl;
import com.github.evgenykuzin.http.models.ClientModel;
import com.github.evgenykuzin.http.models.RequestModel;
import com.github.evgenykuzin.http.models.RequestModelDefault;
import com.github.evgenykuzin.http.models.ResponseModel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.Map;

public class ClosableHttpService implements HttpService<HttpUriRequest, CloseableHttpClient> {

    @Override
    public ClientModel<CloseableHttpClient> constructClient() {
        return () -> HttpClientBuilder.create().build();
    }

    public RequestModel<HttpUriRequest> constructRequest(String url, BodyBuilder bodyBuilder) {
        HeadersModel headers = new HeadersModelImpl("Content-Type", TYPE_JSON);
        return constructRequest(url, POST, headers, bodyBuilder);
    }

    @Override
    public RequestModel<HttpUriRequest> constructRequest(String url, String method, HeadersModel headers, BodyBuilder bodyBuilder) {
        RequestBuilder requestBuilder = null;
        switch (method) {
            case POST : requestBuilder = RequestBuilder.post(); break;
            case GET : requestBuilder = RequestBuilder.get(); break;
            case PUT : requestBuilder = RequestBuilder.put(); break;
            case DELETE : requestBuilder = RequestBuilder.delete(); break;
        }
        if (requestBuilder == null) throw new IllegalArgumentException("http method is not correct (" + method + ")");
        try {
            requestBuilder.setUri(URI.create(url));
            for (Header header : headers.getSet()) {
                requestBuilder.addHeader(header.getKey(), header.getValue());
            }
            requestBuilder.setEntity(new StringEntity(bodyBuilder.getJsonString()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        HttpUriRequest request = requestBuilder.build();
        return new RequestModelDefault<>(
                url,
                request.getRequestLine().toString(),
                bodyBuilder.getJsonString(),
                request
        );
    }

    public RequestModel<HttpUriRequest> constructRequest(String url, String method, HeadersModel headersModel, Map<String, String> params) {
        return constructRequest(url, method, headersModel, (JsonBodyBuilder) (object) -> () -> {
            params.forEach(object::addProperty);
            return object;
        });
    }

    @Override
    public ResponseModel getResponse(RequestModel<HttpUriRequest> request) {
        try {
            CloseableHttpClient client = constructClient().client();
            CloseableHttpResponse response = client.execute(request.getRequest());

            var result = new ResponseModel(
                    readResponse(response),
                    response.getStatusLine().getReasonPhrase(),
                    response.getStatusLine().getStatusCode()
            );
            response.close();
            client.close();
            return result;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ResponseModel.EMPTY;
    }

    private String readResponse(CloseableHttpResponse response) {
        StringBuilder builder = new StringBuilder();
        if (response == null) return null;
        try {
            BufferedReader bufReader = new BufferedReader(
                    new InputStreamReader(
                            response.getEntity().getContent()
                    )
            );
            String line;
            while ((line = bufReader.readLine()) != null) {
                builder.append(line);
                builder.append(System.lineSeparator());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return builder.toString();
    }

}
