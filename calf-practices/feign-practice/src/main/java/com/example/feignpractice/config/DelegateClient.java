package com.example.feignpractice.config;

import feign.Client;
import feign.Request;
import feign.Request.Options;
import feign.Response;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;

/**
 * 代理客户端，请求前答应日志
 */
@Slf4j
public class DelegateClient implements Client {

	private final Client delegate;

	public DelegateClient(Client delegate) {
		this.delegate = delegate;
	}

	@Override
	public Response execute(Request request, Options oldOptions) throws IOException {
        Options options = parseOptions(request, oldOptions);

        log.info("feignHttp request:{}", toString(request));
        Response response = delegate.execute(request, options);

        // 可重复读
        response = response.toBuilder()
                .body(IOUtils.toByteArray(response.body().asReader(), StandardCharsets.UTF_8))
                .build();
        log.info("feignHttp response:{}", toString(response));
        return response;
    }

    private static Options parseOptions(Request request, Options options) {
        request = changeRequest(request);

        Collection<String> connectTimeoutMillisVal = request.headers().remove("$_CONNECT_TIMEOUT_MILLIS");
        Collection<String> readTimeoutMillisVal = request.headers().remove("$_READ_TIMEOUT_MILLIS");

        int connectTimeoutMillis = 0;
        int readTimeoutMillis = 0;
        if (connectTimeoutMillisVal != null) {
            connectTimeoutMillis = Integer.parseInt(connectTimeoutMillisVal.iterator().next());
        }
        if (readTimeoutMillisVal != null) {
            readTimeoutMillis = Integer.parseInt(readTimeoutMillisVal.iterator().next());
        }
        if (connectTimeoutMillis != 0 && readTimeoutMillis != 0) {
            return new Options(connectTimeoutMillis, readTimeoutMillis);
        } else if (connectTimeoutMillis != 0) {
            return new Options(connectTimeoutMillis, options.readTimeoutMillis());
        } else if (readTimeoutMillis != 0) {
            return new Options(options.connectTimeoutMillis(), readTimeoutMillis);
        } else {
            return options;
        }
    }

    private static Request changeRequest(Request request) {
        HashMap<String, Collection<String>> headers = new HashMap<>(request.headers());
        return Request.create(request.httpMethod(), request.url(), headers, request.requestBody());
    }

    public static String toString(Request request) {
        StringBuilder builder = new StringBuilder();
        builder.append(request.httpMethod()).append(' ').append(request.url());

        if (request.requestBody() != null) {
            builder.append(' ').append(request.requestBody().asString());
        }
        return builder.toString();
    }

    public static String toString(Response response) {
        StringBuilder builder = new StringBuilder().append(response.status());
        if (response.reason() != null) {
            builder.append(' ').append(response.reason());
        }

        if (response.body() != null) {
            String rsp = null;
            try {
                rsp = IOUtils.toString(response.body().asReader());
            } catch (IOException ignored) {}
            if (rsp != null) {
                builder.append(' ').append(rsp);
            }
        }

        return builder.toString();
    }
}
