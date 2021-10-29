package com.github.shawven.calf.examples.oauth2.support.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
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
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.http.entity.ContentType.*;


/**
 * HttpClient工具类 异步、任意、连接池线程安全等，超时时间秒为单位
 * <p>创建实例</p>
 * <pre>
 *  // 非连接池
 *  HttpUtil http = new HttpUtil()
 *
 *  // 连接池
 *  HttpUtil http = new HttpUtil(4, 6, 200, 100, 60)
 *  HttpUtil http = HttpUtil.builder()
 *      .setReadTimeout(6)
 *      .setConnectTimeout(4)
 *      .setMaxTotal(200)
 *      .setMaxPerRoute(100)
 *      .setConnectionTimeToLive(60);
 *      .build();
 * </pre>
 * <p>使用实例</p>
 * <pre>
 * http.get("http://www.xx.com")
 * http.post("http://www.xx.com", new HashMap())
 *
 * 参数支持：
 *     Map:
 *         Map<String, Object> payload = new HashMap<>();
 *         payload.put("id", "001");
 *
 *     POJO:
 *         User payload = new User("xxx")
 *
 * post提交:
 *     http.request()
 *             .post("http://www.xx.com")
 *             .setHeader("Content-Type", "application/json")
 *             .setHeader("User-Agent", "Mozilla/5.0")
 *             .setPayload(payload)  // POJO、Map
 *             .setReadTimeout(5)
 *             .setReadTimeout(1)
 *             .execute()
 *
 * multipart提交:
 *     payload.put("file", new File("xx"));
 *     http.request().multipartPost("http://www.xx.com").setPayload(payload).execute()
 *
 * json提交:
 *     http.request().jsonPost("http://www.xx.com").setPayload(payload).execute()
 * </pre>
 * <p>
 * 超时时间都是秒
 *
 * @author Shoven
 * @date 2019-09-23
 */
public class HttpUtil {

    private static final Charset CHARSET = UTF_8;

    private CloseableHttpClient client;

    private static final Gson JSON = new Gson();

    /**
     * 非连接池请求无超时
     */
    public HttpUtil() {
        this(0, 0);
    }

    /**
     * 非连接池请求超时
     *
     * @param connectTimeout 连接超时
     * @param readTimeout    读取超时
     */
    public HttpUtil(int connectTimeout, int readTimeout) {
        this(null, connectTimeout, readTimeout);
    }

    /**
     * 连接池请求无超时
     *
     * @param maxTotal             连接池的最大连接数
     * @param maxPerRoute          连接池每个路由最大连接数
     * @param connectionTimeToLive 连接池连接存活时间
     */
    public HttpUtil(int maxTotal, int maxPerRoute, int connectionTimeToLive) {
        this(null, 0, 0, maxTotal, maxPerRoute, connectionTimeToLive);
    }

    /**
     * 连接池请求超时
     *
     * @param connectTimeout       连接超时
     * @param readTimeout          读取超时
     * @param maxTotal             连接池的最大连接数
     * @param maxPerRoute          连接池每个路由最大连接数
     * @param connectionTimeToLive 连接池连接存活时间
     */
    public HttpUtil(int connectTimeout, int readTimeout, int maxTotal, int maxPerRoute, int connectionTimeToLive) {
        this(null, connectTimeout, readTimeout, connectionTimeToLive, maxTotal, maxPerRoute);
    }

    /**
     * 非连接池SSL请求无超时
     *
     * @param sslContext SSL上下文
     */
    public HttpUtil(SSLContext sslContext) {
        this(sslContext, 0, 0);
    }

    /**
     * 非连接池SSL请求超时
     *
     * @param sslContext     SSL上下文
     * @param connectTimeout 连接超时
     * @param readTimeout    读取超时
     */
    public HttpUtil(SSLContext sslContext, int connectTimeout, int readTimeout) {
        AbstractHttpClientFactory clientFactory = new BasicHttpClientFactory(sslContext);
        this.client = clientFactory.create(connectTimeout, readTimeout);
    }

    /**
     * 连接池SSL请求无超时
     *
     * @param sslContext           SSL上下文
     * @param maxTotal             连接池的最大连接数
     * @param maxPerRoute          连接池每个路由最大连接数
     * @param connectionTimeToLive 连接池连接存活时间
     */
    public HttpUtil(SSLContext sslContext, int maxTotal, int maxPerRoute, int connectionTimeToLive) {
        this(sslContext, 0, 0, maxTotal, maxPerRoute, connectionTimeToLive);
    }

    /**
     * 连接池SSL请求无超时
     *
     * @param sslContext           SSL上下文
     * @param connectTimeout       连接超时
     * @param readTimeout          读取超时
     * @param maxTotal             连接池的最大连接数
     * @param maxPerRoute          连接池每个路由最大连接数
     * @param connectionTimeToLive 连接池连接存活时间
     */
    public HttpUtil(SSLContext sslContext, int connectTimeout, int readTimeout, int maxTotal, int maxPerRoute,
                    int connectionTimeToLive) {
        AbstractHttpClientFactory clientFactory = new PoolingHttpClientFactory(sslContext,
                connectionTimeToLive, maxTotal, maxPerRoute);
        this.client = clientFactory.create(connectTimeout, readTimeout);
    }

    /**
     * 普通GET请求
     *
     * @param url 请求地址
     * @return
     * @throws IOException
     */
    public String get(String url) throws IOException {
        HttpGet httpGet = new HttpGet(url);
        return execute(httpGet);
    }

    /**
     * 普通GET请求
     *
     * @param url            请求地址
     * @param connectTimeOut 连接超时
     * @param readTimeout    读取超时
     * @return
     * @throws IOException
     */
    public String get(String url, int connectTimeOut, int readTimeout) throws IOException {
        HttpGet httpGet = new HttpGet(url);
        return execute(httpGet, connectTimeOut, readTimeout);
    }

    /**
     * 普通GET请求
     *
     * @param url   请求地址
     * @param query 查询参数
     * @return
     * @throws IOException
     */
    public String get(String url, Map<String, ?> query) throws IOException {
        HttpGet httpGet = new HttpGet(Functions.joinUrlAndParams(url, query));
        return execute(httpGet);
    }


    /**
     * 普通GET请求
     *
     * @param url            请求地址
     * @param query          查询参数
     * @param connectTimeOut 连接超时
     * @param readTimeout    读取超时
     * @return
     * @throws IOException
     */
    public String get(String url, Map<String, ?> query, int connectTimeOut, int readTimeout) throws IOException {
        HttpGet httpGet = new HttpGet(Functions.joinUrlAndParams(url, query));
        return execute(httpGet, connectTimeOut, readTimeout);
    }

    /**
     * 普通表单提交POST请求
     *
     * @param url     请求地址
     * @param payload 载荷body
     * @return
     * @throws IOException
     */
    public String post(String url, Map<String, ?> payload) throws IOException {
        HttpPost httpPost = new HttpPost(url);
        httpPost.setEntity(Functions.buildUrlEncodedBody(payload));
        return execute(httpPost);
    }

    /**
     * 普通表单提交POST请求
     *
     * @param url            请求地址
     * @param payload        载荷body
     * @param connectTimeOut 连接超时
     * @param readTimeout    读取超时
     * @return
     * @throws IOException
     */
    public String post(String url, Map<String, ?> payload, int connectTimeOut, int readTimeout) throws IOException {
        HttpPost httpPost = new HttpPost(url);
        httpPost.setEntity(Functions.buildUrlEncodedBody(payload));
        return execute(httpPost, connectTimeOut, readTimeout);
    }

    /**
     * 以JSON提交方式POST请求
     *
     * @param url     请求地址
     * @param payload 载荷body
     * @return
     * @throws IOException
     */
    public String jsonPost(String url, Map<String, ?> payload) throws IOException {
        HttpPost httpPost = new HttpPost(url);
        httpPost.setEntity(Functions.buildJsonBody(payload));
        return execute(httpPost);
    }

    /**
     * 以JSON提交方式POST请求
     *
     * @param url     请求地址
     * @param payload 载荷body
     * @return
     * @throws IOException
     */
    public String jsonPost(String url, Object payload, int connectTimeOut, int readTimeout) throws IOException {
        HttpPost httpPost = new HttpPost(url);
        httpPost.setEntity(Functions.buildJsonBody(payload));
        return execute(httpPost, connectTimeOut, readTimeout);
    }

    /**
     * 执行超时请求
     *
     * @param request        请求
     * @param connectTimeOut 连接超时
     * @param readTimeout    读取超时
     * @return
     * @throws IOException
     */
    private String execute(HttpRequestBase request, int connectTimeOut, int readTimeout) throws IOException {
        RequestConfig requestConfig = Functions.newRequestConfig(connectTimeOut, readTimeout);
        request.setConfig(requestConfig);
        return execute(request);
    }

    /**
     * 执行请求
     *
     * @param request 请求
     * @return
     * @throws IOException
     */
    private String execute(HttpRequestBase request) throws IOException {
        // 关闭连接,释放资源
        try (CloseableHttpResponse response = client.execute(request)) {
            // 执行请求
            HttpEntity entity = response.getEntity();
            return entity == null ? null : EntityUtils.toString(entity, CHARSET);
        }
    }

    /**
     * 获取一个一次性请求
     *
     * @return
     */
    public HttpOnceRequest request() {
        return new HttpOnceRequest();
    }


    /**
     * 一次性请求 非线程安全
     */
    public class HttpOnceRequest {

        private String url;

        private Map<String, Object> payload;

        private List<Header> headers;

        private HttpRequestBase request;

        private ContentType contentType;

        private RequestConfig.Builder requestConfigBuilder;

        HttpOnceRequest() {
            this.headers = new ArrayList<>();
            this.contentType = ContentType.APPLICATION_FORM_URLENCODED.withCharset(CHARSET);

            RequestConfig config = null;
            if (HttpUtil.this.client instanceof Configurable) {
                config = ((Configurable) HttpUtil.this.client).getConfig();
            }
            if (config == null) {
                config = Functions.newRequestConfig();
            }
            this.requestConfigBuilder = RequestConfig.copy(config);
        }

        /**
         * 普通GET请求
         *
         * @return
         */
        public HttpOnceRequest get(String url) {
            this.url = url;
            this.request = new HttpGet();
            return this;
        }


        /**
         * 普通表单提交POST请求
         *
         * @return
         */
        public HttpOnceRequest post(String url) {
            this.url = url;
            this.request = new HttpPost();
            return this;
        }

        /**
         * 普通表单提交PUT请求
         *
         * @return
         */
        public HttpOnceRequest put(String url) {
            this.url = url;
            this.request = new HttpPut();
            return this;
        }


        /**
         * delete请求
         *
         * @return
         */
        public HttpOnceRequest delete(String url) {
            this.url = url;
            this.request = new HttpDelete();
            return this;
        }

        /**
         * JSON方式提交POST请求
         *
         * @return
         */
        public HttpOnceRequest jsonPost(String url) {
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
        public HttpOnceRequest jsonPut(String url) {
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
        public HttpOnceRequest multipartPost(String url) {
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
        public HttpOnceRequest multipartPut(String url) {
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
        public HttpOnceRequest setHeader(String name, String value) {
            this.headers.add(new BasicHeader(name, value));
            return this;
        }

        /**
         * 设置一组http请求头
         *
         * @param headers
         * @return
         */
        public HttpOnceRequest setHeaders(List<Header> headers) {
            this.headers = headers;
            return this;
        }

        /**
         * 设置http请求载荷数据
         *
         * @param payload POJO、Map
         * @return
         */
        public HttpOnceRequest setPayload(Object payload) {
            this.payload = Functions.convertToMap(payload);
            return this;
        }

        public HttpOnceRequest setReadTimeout(int socketTimeout) {
            requestConfigBuilder.setSocketTimeout(socketTimeout);
            return this;
        }

        public HttpOnceRequest setConnectTimeout(int connectTimeout) {
            requestConfigBuilder.setConnectTimeout(connectTimeout);
            return this;
        }

        /**
         * 普通http请求头 chrome浏览器
         *
         * @return
         */
        public HttpOnceRequest withBrowserHeaders() {
            HashMap<String, String> browserHeaders = new HashMap<>(4);
            browserHeaders.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
            browserHeaders.put("Accept-Language", "zh-CN,zh;q=0.8");
            browserHeaders.put("Accept-Encoding", "gzip, deflate, br");
            browserHeaders.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.81 Safari/537.36");
            Collections.addAll(headers, Functions.buildHeaders(browserHeaders));
            return this;
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
            return parseResponse(execute(), responseType);
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
            return parseResponse(execute(), responseType);
        }

        /**
         * 异步执行http请求 返回字符串
         *
         * @return
         * @throws IOException
         */
        public CompletableFuture<String> asyncExecute() {
            return CompletableFuture.supplyAsync(this::nonIoExceptionExecute);
        }

        /**
         * 异步执行http请求 返回指定类型
         *
         * @param responseType
         * @param <R>
         * @return
         */
        public <R> CompletableFuture<R> asyncExecute(Class<R> responseType) {
            return CompletableFuture
                    .supplyAsync(this::nonIoExceptionExecute)
                    .thenApply(s -> parseResponse(s, responseType));
        }

        /**
         * 异步执行http请求 返回指定类型
         *
         * @param responseType
         * @param <R>
         * @return
         */
        public <R> CompletableFuture<R> asyncExecute(TypeToken<R> responseType) {
            return CompletableFuture
                    .supplyAsync(this::nonIoExceptionExecute)
                    .thenApply(s -> parseResponse(s, responseType));
        }


        /**
         * 执行http请求
         *
         * @return
         */
        private String nonIoExceptionExecute() {
            try {
                return this.execute();
            } catch (IOException e) {
                throw new InternalIOException(e);
            }
        }

        /**
         * 执行http请求
         *
         * @return
         */
        public String execute() throws IOException {
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
            return HttpUtil.this.execute(request);
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
        @SuppressWarnings("unchecked")
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
        @SuppressWarnings("unchecked")
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
     * 返回Http工具构造器
     *
     * @return
     */
    public static HttpUtilBuilder builder() {
        return new HttpUtilBuilder();
    }

    /**
     * Http工具构造器
     */
    public static class HttpUtilBuilder {

        /**
         * 连接超时
         */
        private int connectTimeout;

        /**
         * 读取超时
         */
        private int readTimeout;

        /**
         * 连接池连接存活时间
         */
        private int connectionTimeToLive;

        /**
         * 连接池的最大连接数
         */
        private int maxTotal;

        /**
         * 连接池每个路由最大连接数
         */
        private int maxPerRoute;

        /**
         * SSL上下文
         */
        private SSLContext sslContext;

        public HttpUtilBuilder setConnectTimeout(int connectTimeout) {
            this.connectTimeout = connectTimeout;
            return this;
        }

        public HttpUtilBuilder setReadTimeout(int readTimeout) {
            this.readTimeout = readTimeout;
            return this;
        }

        public HttpUtilBuilder setConnectionTimeToLive(int connectionTimeToLive) {
            this.connectionTimeToLive = connectionTimeToLive;
            return this;
        }

        public HttpUtilBuilder setMaxTotal(int maxTotal) {
            this.maxTotal = maxTotal;
            return this;
        }

        public HttpUtilBuilder setMaxPerRoute(int maxPerRoute) {
            this.maxPerRoute = maxPerRoute;
            return this;
        }

        public HttpUtil build() {
            HttpUtil instance;
            if (connectionTimeToLive > 0 || maxTotal > 0 || maxPerRoute > 0) {
                instance = new HttpUtil(sslContext, connectionTimeToLive, maxTotal, maxPerRoute,
                        connectTimeout, readTimeout);
            } else {
                instance = new HttpUtil(sslContext, connectTimeout, readTimeout);
            }
            return instance;
        }

        public HttpUtilBuilder setSslContext(SSLContext sslContext) {
            this.sslContext = sslContext;
            return this;
        }

        /**
         * 设置证书输入流和证书密码生成SSL上下文
         *
         * @param cert
         * @param password
         * @return
         */
        public HttpUtilBuilder setCertificate(InputStream cert, String password) {
            // 证书
            SSLContext sslContext;
            try {
                char[] chars = password.toCharArray();
                KeyStore keyStore = KeyStore.getInstance("PKCS12");
                keyStore.load(cert, chars);
                // 实例化密钥库 & 初始化密钥工厂
                KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
                kmf.init(keyStore, chars);

                // 创建 SSLContext
                sslContext = SSLContext.getInstance("TLS");
                sslContext.init(kmf.getKeyManagers(), null, new SecureRandom());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            this.sslContext = sslContext;
            return this;
        }
    }

    /**
     * Http客户端抽象工厂
     */
    private abstract static class AbstractHttpClientFactory {

        private HttpClientConnectionManager connectionManager;

        public CloseableHttpClient create() {
            return create(0, 0);
        }

        public CloseableHttpClient create(int connectTimeout, int readTimeout) {
            return create(Functions.newRequestConfig(connectTimeout, readTimeout));
        }

        public CloseableHttpClient create(RequestConfig requestConfig) {
            return HttpClients.custom()
                    // 共享连接池对应多个客户端
                    .setConnectionManagerShared(true)
                    // 默认请求配置
                    .setDefaultRequestConfig(requestConfig)
                    .setConnectionManager(connectionManager)
                    .build();
        }

        public void setConnectionManager(HttpClientConnectionManager connectionManager) {
            this.connectionManager = connectionManager;
        }
    }

    /**
     * 基本Http客户端工厂
     */
    private static class BasicHttpClientFactory extends AbstractHttpClientFactory {

        private BasicHttpClientFactory(SSLContext sslContext) {
            init(Functions.newRegistry(sslContext));
        }

        private void init(Registry<ConnectionSocketFactory> registry) {
            BasicHttpClientConnectionManager connectionManager = new BasicHttpClientConnectionManager(registry);
            //JVM 停止或重启时，关闭连接池
            Runtime.getRuntime().addShutdownHook(new Thread(connectionManager::shutdown));
            setConnectionManager(connectionManager);
        }
    }

    /**
     * 连接池Http客户端工厂
     */
    private static class PoolingHttpClientFactory extends AbstractHttpClientFactory {

        private PoolingHttpClientFactory(SSLContext sslContext, int maxTotal, int maxPerRoute,
                                         int connectionTimeToLive) {
            init(Functions.newRegistry(sslContext), maxTotal, maxPerRoute, connectionTimeToLive);
        }

        private void init(Registry<ConnectionSocketFactory> registry, int maxTotal, int maxPerRoute,
                          int connectionTimeToLive) {
            // 连接池
            PoolingHttpClientConnectionManager connectionManager =
                    new PoolingHttpClientConnectionManager(registry, null, null, null,
                            // 连接存活时间，如果不设置，则根据长连接信息决定
                            connectionTimeToLive, TimeUnit.SECONDS);
            // 整个连接池的最大连接数
            connectionManager.setMaxTotal(maxTotal);
            // 每个路由的默认最大连接
            connectionManager.setDefaultMaxPerRoute(maxPerRoute);
            //JVM 停止或重启时，关闭连接池
            Runtime.getRuntime().addShutdownHook(new Thread(connectionManager::shutdown));
            setConnectionManager(connectionManager);
        }
    }

    /**
     * 辅助方法
     */
    public static class Functions {

        public static RequestConfig newRequestConfig() {
            return newRequestConfig(0, 0);
        }

        public static RequestConfig newRequestConfig(int connectTimeout, int readTimeout) {
            return RequestConfig.custom()
                    .setConnectTimeout(connectTimeout * 1000)
                    .setSocketTimeout(readTimeout * 1000)
                    // 从连接池获取连接的等待超时时间
                    .setConnectionRequestTimeout(connectTimeout * 1000)
                    // 忽略cookie
                    .setCookieSpec(CookieSpecs.IGNORE_COOKIES)
                    .build();
        }

        public static Registry<ConnectionSocketFactory> newRegistry(SSLContext sslContext) {
            SSLConnectionSocketFactory sslConnectionSocketFactory = sslContext == null
                    ? SSLConnectionSocketFactory.getSocketFactory()
                    : new SSLConnectionSocketFactory(sslContext);
            return RegistryBuilder.<ConnectionSocketFactory>create()
                    .register("http", PlainConnectionSocketFactory.getSocketFactory())
                    // SSlSocket工厂
                    .register("https", sslConnectionSocketFactory)
                    .build();
        }

        /**
         * 连接url和参数
         *
         * @return
         */
        public static String joinUrlAndParams(String url, Map<String, ?> payload) {
            String queryString = "";
            if (payload != null && !payload.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                int i = 0;
                for (Map.Entry<String, ?> entry : payload.entrySet()) {
                    String key = entry.getKey();
                    Object value = entry.getValue();
                    if (isValidPair(key, value)) {
                        sb.append(i++ == 0 ? "?" : "&").append(entry.getKey()).append("=").append(entry.getValue());
                    }
                }
                queryString = sb.toString();
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
         * @param headers
         * @return
         */
        public static Header[] buildHeaders(Map<String, String> headers) {
            if (headers == null || headers.isEmpty()) {
                return null;
            }
            Set<Header> headerSet = new HashSet<>();
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                if (isValidPair(key, value)) {
                    headerSet.add(new BasicHeader(key, value));
                }
            }
            return headerSet.toArray(new Header[]{});
        }

        /**
         * 构建Multipart/form-data参数
         *
         * @param payload
         * @return
         */
        public static HttpEntity buildMultipartBody(Map<String, ?> payload) {
            if (payload == null || payload.isEmpty()) {
                return null;
            }
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            ContentType contentType = MULTIPART_FORM_DATA.withCharset(CHARSET);
            builder.setContentType(contentType);

            for (Map.Entry<?, ?> entry : payload.entrySet()) {
                String key = String.valueOf(entry.getKey());
                Object value = entry.getValue();
                if (isNotBlank(key) || value == null) {
                    continue;
                }

                if (value instanceof String) {
                    if (!isNotBlank(value.toString())) {
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
        }

        public static Map<String, Object> convertToMap(Object input) {
            if (input == null) {
                return null;
            }
            Map<String, Object> result;
            if (input instanceof Map) {
                Map<?, ?> source = (Map<?, ?>) input;
                result = new LinkedHashMap<>(source.size());
                source.forEach((key, value) -> result.put(String.valueOf(key), value));
            } else {
                BeanInfo info;
                try {
                    info = Introspector.getBeanInfo(input.getClass(), Object.class);
                    PropertyDescriptor[] pds = info.getPropertyDescriptors();
                    result = new LinkedHashMap<>(pds.length);
                    for (PropertyDescriptor pd : pds) {
                        String key = pd.getName();
                        Object value = pd.getReadMethod().invoke(input);
                        result.put(key, value);
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            return result;
        }

        /**
         * 构建表单参数
         *
         * @param payload POJO 、Map
         * @return
         */
        public static StringEntity buildUrlEncodedBody(Map<String, ?> payload) {
            if (payload == null || payload.isEmpty()) {
                return null;
            }

            List<NameValuePair> forms = new ArrayList<>();
            for (Map.Entry<String, ?> entry : payload.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                if (isValidPair(key, value)) {
                    forms.add(new BasicNameValuePair(key, value.toString()));
                }
            }
            return new UrlEncodedFormEntity(forms, CHARSET);
        }

        /**
         * 构建JSON参数
         *
         * @param payload POJO 、Map
         * @return StringEntity
         */
        public static StringEntity buildJsonBody(Object payload) {
            return new StringEntity(JSON.toJson(payload), APPLICATION_JSON.withCharset(CHARSET));
        }

        /**
         * 有效键值对
         *
         * @param key
         * @param value
         * @return
         */
        public static boolean isValidPair(String key, Object value) {
            return isNotBlank(key) && value != null && isNotBlank(value.toString());
        }

        /**
         * 是否空白字符串
         *
         * @param cs
         * @return
         */
        public static boolean isNotBlank(CharSequence cs) {
            int strLen;
            if (cs != null && (strLen = cs.length()) != 0) {
                for (int i = 0; i < strLen; ++i) {
                    if (!Character.isWhitespace(cs.charAt(i))) {
                        return true;
                    }
                }
                return false;
            } else {
                return false;
            }
        }
    }

    /**
     * 包装的IO异常
     */
    private static class InternalIOException extends RuntimeException {
        private static final long serialVersionUID = -6984037130236738749L;

        InternalIOException(Throwable cause) {
            super(cause);
        }
    }
}
