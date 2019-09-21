package com.test.app.support.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.http.entity.ContentType.*;


/**
 * HttpClient工具类
 *
 * @author Shoven
 * @date 2019-08-26
 */
public class HttpClientUtils {

    private static final Charset CHARSET = UTF_8;

    private static final Gson JSON = new Gson();

    /**
     * 普通GET请求
     *
     * @param url
     * @return
     * @throws IOException
     */
    public static String get(String url) throws IOException {
        HttpGet httpGet = new HttpGet(url);
        return HttpExecutor.execute(httpGet);
    }

    /**
     * 普通GET请求
     *
     * @param url
     * @param query POJO 、Map 、String
     * @return
     * @throws IOException
     */
    public static String get(String url, Object query) throws IOException {
        HttpGet httpGet = new HttpGet(Functions.joinUrlAndParams(url, query));
        return HttpExecutor.execute(httpGet);
    }

    /**
     * 普通GET请求
     *
     * @param query
     * @param headers
     * @param query   POJO 、Map 、String
     * @return
     * @throws IOException
     */
    public static String get(String url, Map<String, String> headers, Object query) throws IOException {
        HttpGet httpGet = new HttpGet(Functions.joinUrlAndParams(url, query));
        httpGet.setHeaders(Functions.buildHeaders(headers));
        return HttpExecutor.execute(httpGet);
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
        httpPost.setEntity(Functions.buildUrlEncodedBody(payload));
        return HttpExecutor.execute(httpPost);
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
        httpPost.setEntity(Functions.buildUrlEncodedBody(payload));
        httpPost.setHeaders(Functions.buildHeaders(headers));
        return HttpExecutor.execute(httpPost);
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
        httpPost.setEntity(Functions.buildJsonBody(payload));
        return HttpExecutor.execute(httpPost);
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
        httpPost.setEntity(Functions.buildJsonBody(payload));
        httpPost.setHeaders(Functions.buildHeaders(headers));
        return HttpExecutor.execute(httpPost);
    }

    /**
     * 获得Http客户端执行器
     *
     * @return HttpClientExecutor
     */
    public static HttpClientExecutor executor() {
        return new HttpClientExecutor();
    }

    /**
     * Http客户端执行器
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
            this.requestConfigBuilder = RequestConfig.copy(HttpExecutor.REQUEST_CONFIG);
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
        public HttpClientExecutor jsonPost(String url) {
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
            Collections.addAll(headers, Functions.buildHeaders(browserHeaders));
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
        public <R> CompletableFuture<R> asyncExecute(TypeToken<R> responseType) {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    return this.doExecute();
                } catch (IOException e) {
                    throw new WrappedIOException(e);
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
                    request.setURI(URI.create(Functions.joinUrlAndParams(url, payload)));
                    break;
                case HttpPost.METHOD_NAME:
                case HttpPut.METHOD_NAME:
                    ((HttpEntityEnclosingRequestBase) request).setEntity(getEntity());
                    break;
                case HttpDelete.METHOD_NAME:
                default:
            }

            if (!headers.isEmpty()) {
                request.setHeaders(headers.toArray(new Header[]{}));
            }
            request.setConfig(requestConfigBuilder.build());
            return HttpExecutor.execute(request);
        }

        /**
         * 获取请求实体
         *
         * @return
         */
        private HttpEntity getEntity() {
            String mimeType = contentType.getMimeType();
            if (mimeType.equals(APPLICATION_JSON.getMimeType())) {
                return Functions.buildJsonBody(payload);
            }
            if (mimeType.equals(MULTIPART_FORM_DATA.getMimeType())) {
                return Functions.buildMultipartBody(payload);
            }
            return Functions.buildUrlEncodedBody(payload);
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
                return (R) str;
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
                return (R) str;
            }
            return JSON.fromJson(str, responseType.getType());
        }
    }

    /**
     * Http执行器
     */
    private static class HttpExecutor {

        private static final int SOCKET_TIMEOUT = 5;

        private static final int CONNECT_TIMEOUT = 2;

        private static final int CONNECTION_TIME_TO_LIVE = 60;

        private static final int MAX_TOTAL = 200;

        private static final int MAX_PER_ROUTE = 100;

        private static final CloseableHttpClient CLIENT;

        private static final RequestConfig REQUEST_CONFIG;

        static {
            SSLConnectionSocketFactory sslConnectionSocketFactory = SSLConnectionSocketFactory.getSocketFactory();

            REQUEST_CONFIG = RequestConfig.custom()
                    // 连接超时时间
                    .setConnectTimeout(CONNECT_TIMEOUT * 1000)
                    // 等待数据超时时间
                    .setSocketTimeout(SOCKET_TIMEOUT * 1000)
                    // 从连接池获取连接的等待超时时间
                    .setConnectionRequestTimeout(CONNECT_TIMEOUT * 1000)
                    // 忽略cookie
                    .setCookieSpec(CookieSpecs.IGNORE_COOKIES)
                    .build();

            CLIENT = HttpClients.custom()
                    // 默认请求配置
                    .setDefaultRequestConfig(REQUEST_CONFIG)
                    // SSlSocket工厂
                    .setSSLSocketFactory(sslConnectionSocketFactory)
                    // 连接存活时间，如果不设置，则根据长连接信息决定
                    .setConnectionTimeToLive(CONNECTION_TIME_TO_LIVE, TimeUnit.SECONDS)
                    // 整个连接池的最大连接数
                    .setMaxConnTotal(MAX_TOTAL)
                    // 每个路由的默认最大连接
                    .setMaxConnPerRoute(MAX_PER_ROUTE)
                    .build();

            //JVM 停止或重启时，关闭连接池释放掉连接
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try{
                    if(CLIENT != null){
                        CLIENT.close();
                    }
                } catch (IOException e){
                    e.printStackTrace();
                }
            }));
        }

        private static String execute(HttpRequestBase request) throws IOException {
            // 关闭连接,释放资源
            try (CloseableHttpResponse response = CLIENT.execute(request)) {
                // 执行请求
                HttpEntity entity = response.getEntity();
                return entity == null ? null : EntityUtils.toString(entity, CHARSET);
            }
        }
    }

    /**
     * 辅助方法
     */
    public static class Functions {
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
         * @param header POJO 、Map
         * @return
         */
        private static Header[] buildHeaders(Object header) {
            return convertMapOrPojo(header, input -> {
                if (input == null || input.isEmpty()) {
                    return null;
                }
                Set<Header> headers = new HashSet<>();
                for (Map.Entry<String, Object> entry : input.entrySet()) {
                    String key = entry.getKey();
                    Object value = entry.getValue();
                    if (isValidPair(key, value)) {
                        headers.add(new BasicHeader(key, value.toString()));
                    }
                }
                return headers.toArray(new Header[]{});
            });
        }

        /**
         * 构建url参数
         *
         * @param payload POJO 、Map
         * @return 格式:key1=value1&key2=value2
         */
        private static String buildQueryString(Object payload) {
            if (payload instanceof String) {
                return (String) payload;
            }

            return convertMapOrPojo(payload, input -> {
                if (input == null || input.isEmpty()) {
                    return "";
                }
                StringBuilder sb = new StringBuilder();
                int i = 0;
                for (Map.Entry<String, Object> entry : input.entrySet()) {
                    String key = entry.getKey();
                    Object value = entry.getValue();
                    if (isValidPair(key, value)) {
                        sb.append(i++ == 0 ? "?" : "&").append(entry.getKey()).append("=").append(entry.getValue());
                    }
                }
                return sb.toString();
            });
        }

        /**
         * 构建表单参数
         *
         * @param payload POJO 、Map
         * @return
         */
        private static StringEntity buildUrlEncodedBody(Object payload) {
            return convertMapOrPojo(payload, input -> {
                if (input == null || input.isEmpty()) {
                    return null;
                }

                List<NameValuePair> forms = new ArrayList<>();
                for (Map.Entry<String, Object> entry : input.entrySet()) {
                    String key = entry.getKey();
                    Object value = entry.getValue();
                    if (isValidPair(key, value)) {
                        forms.add(new BasicNameValuePair(key, value.toString()));
                    }
                }
                return new UrlEncodedFormEntity(forms, CHARSET);
            });
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
            Function<? super Map<String, Object>, HttpEntity> converter = input -> {
                if (input == null || input.isEmpty()) {
                    return null;
                }

                MultipartEntityBuilder builder = MultipartEntityBuilder.create();
                ContentType contentType = MULTIPART_FORM_DATA.withCharset(CHARSET);
                builder.setContentType(contentType);

                for (Map.Entry entry : input.entrySet()) {
                    String key = String.valueOf(entry.getKey());
                    Object value = entry.getValue();
                    if (isBlankString(key) || value == null) {
                        continue;
                    }

                    if (value instanceof String) {
                        if (isBlankString(value.toString())) {
                            continue;
                        }
                        builder.addTextBody(key, value.toString(), contentType);
                    } else {
                        if (value instanceof File) {
                            builder.addBinaryBody(key, (File) value);
                            continue;
                        }
                        // 没有文件名获取不到该文件
                        String fileName = System.currentTimeMillis() + ""
                                + (int) ((Math.random() * 9 + 1) * 10000);
                        if (value instanceof byte[]) {
                            builder.addBinaryBody(key, (byte[]) value, DEFAULT_BINARY, fileName);
                        }
                        if (value instanceof InputStream) {
                            builder.addBinaryBody(key, (InputStream) value, DEFAULT_BINARY, fileName);
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
         * @param input     POJO 、Map
         * @param converter 转换器
         * @param <T>
         * @return
         */
        private static <T> T convertMapOrPojo(Object input, Function<? super Map<String, Object>, T> converter) {
            if (input == null) {
                return null;
            }
            Map<String, Object> map;
            if (input instanceof Map) {
                Map<Object, Object> source = (Map<Object, Object>)input;
                map = new LinkedHashMap<>(source.size());
                source.forEach((key, value) -> map.put(String.valueOf(key), value));
            } else {
                BeanInfo info;
                try {
                    info = Introspector.getBeanInfo(input.getClass(), Object.class);
                    PropertyDescriptor[] pds = info.getPropertyDescriptors();
                    map = new LinkedHashMap<>(pds.length);
                    for (PropertyDescriptor pd : pds) {
                        String key = pd.getName();
                        Object value = pd.getReadMethod().invoke(input, map.get(pd.getName()));
                        map.put(key, value);
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            return converter.apply(map);
        }

        private static boolean isValidPair(String key, Object value) {
            return !isBlankString(key) && value != null && !isBlankString(value.toString());
        }

        private static boolean isBlankString(CharSequence cs) {
            int strLen;
            if (cs != null && (strLen = cs.length()) != 0) {
                for (int i = 0; i < strLen; ++i) {
                    if (!Character.isWhitespace(cs.charAt(i))) {
                        return false;
                    }
                }
                return true;
            } else {
                return true;
            }
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
