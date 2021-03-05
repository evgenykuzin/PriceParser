package org.jekajops.http.models;

public class RequestModelDefault<T> extends RequestModel<T> {
    private final T request;
    public RequestModelDefault(String url, String requestString, String jsonBody, T request) {
        super(url, requestString, jsonBody);
        this.request = request;
    }

    @Override
    public T getRequest() {
        return request;
    }
}
