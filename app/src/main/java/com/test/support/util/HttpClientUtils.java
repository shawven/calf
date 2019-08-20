package com.test.support.util;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.http.entity.ContentType.APPLICATION_JSON;
import static org.apache.http.entity.ContentType.MULTIPART_FORM_DATA;


/**
 * HttpClient工具类
 *
 * @author Shoven
 * @date 2018-10-12 15:30
 */
public class HttpClientUtils {
    private static Logger logger = LoggerFactory.getLogger(HttpClientUtils.class);

    private static final int TIMEOUT = 6;
    private static final int MAX_TOTAL = 200;
    private static final int MAX_PER_ROUTE = 100;
    private static final Charset CHARSET = UTF_8;
    private static final Gson JSON = new Gson();
    private static final String HTTP = "http";
    private static final String HTTPS = "https";
    private static final String[] SUPPORTED_PROTOCOLS = {"SSLv2Hello", "SSLv3", "TLSv1", "TLSv1.2"};

    private static RequestConfig defaultRequestConfig;
    private static PoolingHttpClientConnectionManager defaultPoolingHttpClientConnectionManager;
    private static SSLConnectionSocketFactory defaultSSLConnectionSocketFactory;

    static {
        try {
            // 全部信任 不做身份鉴定
            SSLContext sslcontext = SSLContexts.custom()
                    .loadTrustMaterial(null, (TrustStrategy) (x509Certificates, s) -> true)
                    .build();

            defaultSSLConnectionSocketFactory = new SSLConnectionSocketFactory(sslcontext, SUPPORTED_PROTOCOLS, null,
                    NoopHostnameVerifier.INSTANCE);

            Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                    .register(HTTP, PlainConnectionSocketFactory.INSTANCE)
                    .register(HTTPS, defaultSSLConnectionSocketFactory)
                    .build();

            defaultPoolingHttpClientConnectionManager = new PoolingHttpClientConnectionManager(registry);
            defaultPoolingHttpClientConnectionManager.setMaxTotal(MAX_TOTAL);
            defaultPoolingHttpClientConnectionManager.setDefaultMaxPerRoute(MAX_PER_ROUTE);

            defaultRequestConfig = RequestConfig.custom()
                    .setSocketTimeout(TIMEOUT * 1000)
                    .setConnectTimeout(TIMEOUT * 1000)
                    .setConnectionRequestTimeout(1000)
                    .setCookieSpec(CookieSpecs.IGNORE_COOKIES)
                    .build();
        } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * 普通GET请求
     *
     * @param url
     * @return
     * @throws IOException
     */
    public static String get(String url) throws IOException {
        HttpGet httpGet = new HttpGet(url);
        return execute(httpGet);
    }

    /**
     * 普通GET请求
     *
     * @param url
     * @param query  POJO 、Map 、String
     * @return
     * @throws IOException
     */
    public static String get(String url, Object query) throws IOException {
        HttpGet httpGet = new HttpGet(joinUrlAndParams(url, query));
        return execute(httpGet);
    }

    /**
     * 普通GET请求
     *
     * @param query
     * @param headers
     * @param query POJO 、Map 、String
     * @return
     * @throws IOException
     */
    public static String get(String url, Map<String, String> headers, Object query) throws IOException {
        HttpGet httpGet = new HttpGet(joinUrlAndParams(url, query));
        httpGet.setHeaders(buildHeaders(headers));
        return execute(httpGet);
    }

    /**
     * 普通表单提交POST请求
     *
     * @param url
     * @param payload POJO 、Map
     * @return
     * @throws IOException
     */
    public static String post(String url, Object payload) throws IOException {
        HttpPost httpPost = new HttpPost(url);
        httpPost.setEntity(buildUrlEncodedBody(payload));
        return execute(httpPost);
    }

    /**
     * 普通表单提交POST请求
     *
     * @param url
     * @param headers
     * @param payload POJO 、Map
     * @return
     * @throws IOException
     */
    public static String post(String url, Map<String, String> headers, Object payload) throws IOException {
        HttpPost httpPost = new HttpPost(url);
        httpPost.setEntity(buildUrlEncodedBody(payload));
        httpPost.setHeaders(buildHeaders(headers));
        return execute(httpPost);
    }

    /**
     * 以JSON提交方式POST请求
     *
     * @param url
     * @param payload POJO 、Map
     * @return
     * @throws IOException
     */
    public static String jsonPost(String url, Object payload) throws IOException {
        HttpPost httpPost = new HttpPost(url);
        httpPost.setEntity(buildJsonBody(payload));
        return execute(httpPost);
    }

    /**
     * 以JSON提交方式POST请求
     *
     * @param url
     * @param headers
     * @param payload POJO 、Map
     * @return
     * @throws IOException
     */
    public static String jsonPost(String url, Map<String, String> headers, Object payload) throws IOException {
        HttpPost httpPost = new HttpPost(url);
        httpPost.setEntity(buildJsonBody(payload));
        httpPost.setHeaders(buildHeaders(headers));
        return execute(httpPost);
    }

    /**
     * 发起http请求
     *
     * @param request
     * @param <T>
     * @return
     * @throws IOException
     */
    public static <T extends HttpRequestBase> String execute(T request) throws IOException {
        return execute(getDefaultClient(), request);
    }

    /**
     * 发起http请求
     *
     * @param httpClient
     * @param request
     * @param <T>
     * @return
     * @throws IOException
     */
    public static <T extends HttpRequestBase> String execute(CloseableHttpClient httpClient,
                                                             T request) throws IOException {
        // 关闭连接,释放资源
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            // 执行请求
            HttpEntity entity = response.getEntity();
            return entity == null ? null : EntityUtils.toString(entity, CHARSET);
        }
    }

    /**
     * 获取默认客户端
     *
     * @return
     */
    public static CloseableHttpClient getDefaultClient() {
        return getClient(defaultRequestConfig, defaultPoolingHttpClientConnectionManager,
                defaultSSLConnectionSocketFactory);
    }

    /**
     * 获取客户端
     *
     * @param requestConfig
     * @param manager
     * @param connectionSocketFactory
     * @return
     */
    private static CloseableHttpClient getClient(RequestConfig requestConfig,
                                                 HttpClientConnectionManager manager,
                                                 LayeredConnectionSocketFactory connectionSocketFactory) {
        return HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .setConnectionManager(manager)
                .setSSLSocketFactory(connectionSocketFactory)
                .setConnectionTimeToLive(TIMEOUT, TimeUnit.SECONDS)
                .build();
    }

    /**
     * 连接url和参数
     *
     * @return
     */
    private static String joinUrlAndParams(String url, Object payload) {
        String queryString = buildQueryString(payload);
        if (queryString == null) {
            return url;
        }
        StringBuilder fullUrl = new StringBuilder(url);
        if (Pattern.matches(".+\\?.+", url)) {
            fullUrl.append("&").append(queryString.substring(1));
        } else {
            fullUrl.append(queryString);
        }
        return fullUrl.toString();
    }

    /**
     * 构建http请求头
     *
     * @param header  POJO 、Map
     * @return
     */
    private static Header[] buildHeaders(Object header) {
        Function<? super Map, Header[]> converter = input -> {
            Map<Object, Object> map = (Map<Object, Object>) input;
            if (MapUtils.isEmpty(map)) {
                return null;
            }
            Set<Header> headers  = new HashSet<>();
            for (Map.Entry entry : map.entrySet()) {
                String key = String.valueOf(entry.getKey());
                Object value = entry.getValue();
                if (StringUtils.isBlank(key) || value == null || StringUtils.isBlank(value.toString())) {
                    continue;
                }
                headers.add(new BasicHeader(key, value.toString()));
            }
            return headers.toArray(new Header[]{});
        };

        return convertMapOrPojo(header, converter);
    }

    /**
     * 构建url参数
     *
     * @param payload POJO 、Map
     * @return  格式:key1=value1&key2=value2
     */
    public static String buildQueryString(Object payload) {
        if (payload instanceof String) {
            return (String)payload;
        }

        Function<? super Map, String> converter = input -> {
            Map<Object, Object> map = (Map<Object, Object>) input;
            if (MapUtils.isEmpty(map)) {
                return "";
            }
            StringBuilder sb = new StringBuilder();
            int i = 0;
            for (Map.Entry entry : map.entrySet()) {
                String key = String.valueOf(entry.getKey());
                Object value = entry.getValue();
                if (StringUtils.isBlank(key) || value == null || StringUtils.isBlank(value.toString())) {
                    continue;
                }
                sb.append(i++ == 0 ? "?" : "&").append(entry.getKey()).append("=").append(entry.getValue());
            }
            return sb.toString();
        };

        return convertMapOrPojo(payload, converter);
    }

    /**
     * 构建表单参数
     *
     * @param payload  POJO 、Map
     * @return
     */
    private static StringEntity buildUrlEncodedBody(Object payload) {
        Function<? super Map, StringEntity> converter = input -> {
            Map<Object, Object> map = (Map<Object, Object>) input;
            if (MapUtils.isEmpty(map)) {
                return null;
            }

            List<NameValuePair> forms  = new ArrayList<>();
            for (Map.Entry entry : map.entrySet()) {
                String key = String.valueOf(entry.getKey());
                Object value = entry.getValue();
                if (StringUtils.isBlank(key) || value == null || StringUtils.isBlank(value.toString())) {
                    continue;
                }
                forms.add(new BasicNameValuePair(key, value.toString()));
            }
            return new UrlEncodedFormEntity(forms , CHARSET);
        };

        return convertMapOrPojo(payload, converter);
    }


    /**
     * 构建JSON参数
     *
     * @param payload POJO 、Map
     * @return StringEntity
     */
    private static StringEntity buildJsonBody(Object payload) {
        return new StringEntity(JSON.toJson(payload), APPLICATION_JSON.withCharset(CHARSET));
    }

    /**
     * 构建Multipart/form-data参数
     *
     * @param payload
     * @return
     */
    private static HttpEntity buildMultipartBody(Object payload) {
        Function<? super Map, HttpEntity> converter = input -> {
            Map<Object, Object> map = (Map<Object, Object>) input;
            if (MapUtils.isEmpty(map)) {
                return null;
            }

            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            ContentType contentType = MULTIPART_FORM_DATA.withCharset(CHARSET);
            builder.setContentType(contentType);

            for (Map.Entry entry : map.entrySet()) {
                String key = String.valueOf(entry.getKey());
                Object value = entry.getValue();
                if (StringUtils.isBlank(key) || value == null) {
                    continue;
                }

                if (value instanceof String) {
                    if (StringUtils.isBlank(value.toString())) {
                        continue;
                    }
                    builder.addTextBody(key, value.toString(), contentType);
                } else {
                    if (value instanceof File) {
                        builder.addBinaryBody(key, (File)value);
                        continue;
                    }
                    // 没有文件名获取不到该文件
                    String fileName = System.currentTimeMillis() + ""
                            + (int)((Math.random() * 9 + 1) * 10000);
                    if (value instanceof byte[]) {
                        builder.addBinaryBody(key, (byte[])value, ContentType.DEFAULT_BINARY, fileName);
                    }
                    if (value instanceof InputStream) {
                        builder.addBinaryBody(key, (InputStream)value, ContentType.DEFAULT_BINARY, fileName);
                    }
                }
            }
            return builder.build();
        };

        return convertMapOrPojo(payload, converter);
    }

    /**
     * 使用转换器转换输入成统一的map类型
     *
     * @param input  POJO 、Map
     * @param converter 转换器
     * @param <T>
     * @return
     */
    private static <T> T convertMapOrPojo(Object input, Function<? super Map, T> converter) {
        if (input == null) {
            return null;
        }

        Map mapInput;
        if (input instanceof Map) {
            mapInput = (Map)input;
        } else {
            Set<Map.Entry<String, JsonElement>> entries = JSON.toJsonTree(input).getAsJsonObject().entrySet();
            mapInput = new LinkedHashMap<>(entries.size());
            entries.forEach(entry -> mapInput.put(entry.getKey(), entry.getValue().getAsString()));
        }
        return converter.apply(mapInput);
    }

    /**
     * 返回执行器
     *
     * @return HttpClientExecutor
     */
    public static HttpClientExecutor executor() {
        return new HttpClientExecutor();
    }

    /**
     * HttpClient执行器
     */
    public static class HttpClientExecutor {
        private String url;

        private Object payload;

        private List<Header> headers;

        private HttpRequestBase request;

        private ContentType contentType;

        private RequestConfig.Builder requestConfigBuilder;

        HttpClientExecutor() {
            this.headers = new ArrayList<>();
            this.contentType = ContentType.APPLICATION_FORM_URLENCODED.withCharset(CHARSET);
            this.requestConfigBuilder =  RequestConfig.copy(defaultRequestConfig);
        }

        /**
         * 普通GET请求
         *
         * @return
         */
        public HttpClientExecutor get(String url) {
            this.url = url;
            this.request = new HttpGet();
            return this;
        }


        /**
         * 普通表单提交POST请求
         *
         * @return
         */
        public HttpClientExecutor post(String url) {
            this.url = url;
            this.request = new HttpPost();
            return this;
        }

        /**
         * 普通表单提交PUT请求
         *
         * @return
         */
        public HttpClientExecutor put(String url) {
            this.url = url;
            this.request = new HttpPut();
            return this;
        }


        /**
         * delete请求
         *
         * @return
         */
        public HttpClientExecutor delete(String url) {
            this.url = url;
            this.request = new HttpDelete();
            return this;
        }

        /**
         * JSON方式提交POST请求
         *
         * @return
         */
        public HttpClientExecutor jsonPost(String url)  {
            this.url = url;
            this.request = new HttpPost();
            this.contentType = APPLICATION_JSON.withCharset(CHARSET);
            return this;
        }

        /**
         * JSON方式提交PUT请求
         *
         * @return
         */
        public HttpClientExecutor jsonPut(String url) {
            this.url = url;
            this.request = new HttpPut();
            this.contentType = APPLICATION_JSON.withCharset(CHARSET);
            return this;
        }

        /**
         * multipart/form-data方式提交POST请求
         *
         * @return
         */
        public HttpClientExecutor multipartPost(String url) {
            this.url = url;
            this.request = new HttpPost();
            this.contentType = MULTIPART_FORM_DATA.withCharset(CHARSET);
            return this;
        }

        /**
         * multipart/form-data方式提交PUT请求
         *
         * @return
         */
        public HttpClientExecutor multipartPut(String url) {
            this.url = url;
            this.request = new HttpPut();
            this.contentType = MULTIPART_FORM_DATA.withCharset(CHARSET);
            return this;
        }

        /**
         * 设置请求头
         *
         * @param name
         * @param value
         * @return
         */
        public HttpClientExecutor setHeader(String name, String value) {
            this.headers.add(new BasicHeader(name, value));
            return this;
        }

        /**
         * 设置一组http请求头
         *
         * @param headers
         * @return
         */
        public HttpClientExecutor setHeaders(List<Header> headers) {
            this.headers = headers;
            return this;
        }

        /**
         * 设置http请求载荷数据
         *
         * @param payload POJO、Map
         * @return
         */
        public HttpClientExecutor setPayload(Object payload) {
            this.payload = payload;
            return this;
        }

        public HttpClientExecutor setReadTimeOut(int socketTimeOut) {
            requestConfigBuilder.setSocketTimeout(socketTimeOut);
            return this;
        }

        public HttpClientExecutor setConnectTimeOut(int connectTimeOut) {
            requestConfigBuilder.setConnectTimeout(connectTimeOut);
            return this;
        }

        /**
         * 普通http请求头 chrome浏览器
         *
         * @return
         */
        public HttpClientExecutor withBrowserHeaders() {
            HashMap<String, String> browserHeaders = new HashMap<>(4);
            browserHeaders.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
            browserHeaders.put("Accept-Language", "zh-CN,zh;q=0.8");
            browserHeaders.put("Accept-Encoding", "gzip, deflate, br");
            browserHeaders.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.81 Safari/537.36");
            Collections.addAll(headers, buildHeaders(browserHeaders));
            return this;
        }

        /**
         * 执行http请求 返回字符串
         *
         * @return
         * @throws IOException
         */
        public String execute() throws IOException {
            return doExecute();
        }

        /**
         * 执行http请求 返回指定类型
         *
         * @param responseType
         * @param <R>
         * @return
         * @throws IOException
         */
        public <R> R execute(Class<R> responseType) throws IOException {
            return parseResponse(doExecute(), responseType);
        }

        /**
         * 执行http请求 返回指定类型
         *
         * @param responseType
         * @param <R>
         * @return
         * @throws IOException
         */
        public <R> R execute(TypeToken<R> responseType) throws IOException {
            return parseResponse(doExecute(), responseType);
        }

        /**
         * 异步执行http请求 返回字符串
         *
         * @return
         * @throws IOException
         */
        public CompletableFuture<String> asyncExecute() {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    return this.doExecute();
                } catch (IOException e) {
                    throw new WrappedIOException(e);
                }
            });
        }

        /**
         * 异步执行http请求 返回指定类型
         *
         * @param responseType
         * @param <R>
         * @return
         */
        public <R> CompletableFuture<R> asyncExecute(Class<R> responseType) {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    return this.doExecute();
                } catch (IOException e) {
                    throw new WrappedIOException(e);
                }
            }).thenApply(s -> parseResponse(s, responseType));
        }

        /**
         * 异步执行http请求 返回指定类型
         *
         * @param responseType
         * @param <R>
         * @return
         */
        public <R>  CompletableFuture<R> asyncExecute(TypeToken<R> responseType) {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    return this.doExecute();
                } catch (IOException e) {
                    throw new WrappedIOException( e);
                }
            }).thenApply(s -> parseResponse(s, responseType));
        }


        /**
         * 执行http请求
         *
         * @return
         */
        private String doExecute() throws IOException {
            request.setURI(URI.create(url));
            switch (request.getMethod()) {
                case HttpGet.METHOD_NAME:
                    request.setURI(URI.create(joinUrlAndParams(url, payload)));
                    break;
                case HttpPost.METHOD_NAME:
                case HttpPut.METHOD_NAME:
                    ((HttpEntityEnclosingRequestBase)request).setEntity(getEntity());
                    break;
                case HttpDelete.METHOD_NAME:
                default:
            }

            if (!headers.isEmpty()) {
                request.setHeaders(headers.toArray(new Header[]{}));
            }

            CloseableHttpClient client = getClient(requestConfigBuilder.build(),
                    defaultPoolingHttpClientConnectionManager, defaultSSLConnectionSocketFactory);
            return HttpClientUtils.execute(client, request);
        }

        /**
         * 获取请求实体
         *
         * @return
         */
        private HttpEntity getEntity() {
            String mimeType = contentType.getMimeType();
            if (mimeType.equals(APPLICATION_JSON.getMimeType())) {
                return buildJsonBody(payload);
            }
            if (mimeType.equals(MULTIPART_FORM_DATA.getMimeType())) {
                return buildMultipartBody(payload);
            }

            return buildUrlEncodedBody(payload);
        }

        /**
         * 根据Class解析响应
         *
         * @param str
         * @param responseType
         * @param <R>
         * @return
         */
        private <R> R parseResponse(String str, Class<R> responseType) {
            if (str == null) {
                return null;
            }
            if (responseType.isAssignableFrom(String.class)) {
                return (R)str;
            }
            return JSON.fromJson(str, responseType);
        }

        /**
         * 根据TypeToken解析响应
         *
         * @param str
         * @param responseType
         * @param <R>
         * @return
         */
        private <R> R parseResponse(String str, TypeToken<R> responseType) {
            if (str == null) {
                return null;
            }
            if (responseType.getRawType().isAssignableFrom(String.class)) {
                return (R)str;
            }
            return JSON.fromJson(str, responseType.getType());
        }
    }

    /**
     * 包装的IO异常
     */
    private static class WrappedIOException extends RuntimeException {

        public WrappedIOException(Throwable cause) {
            super(cause);
        }
    }
}
