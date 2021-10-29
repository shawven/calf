package com.github.shawven.calf.payment.support;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_8;


/**
 * HttpClient工具类
 * <p>线程安全、涉及的超时时间都是秒</p>
 * <p>创建实例</p>
 * <pre>
 *  // 非连接池
 *  HttpUtil http = new HttpUtil()
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
 * @author Shoven
 * @date 2019-09-23
 */
public class HttpUtil {

    private static final Charset CHARSET = UTF_8;

    private CloseableHttpClient client;

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
    public String execute(HttpRequestBase request) throws IOException {
        // 关闭连接,释放资源
        try (CloseableHttpResponse response = client.execute(request)) {
            // 执行请求
            HttpEntity entity = response.getEntity();
            return entity == null ? null : EntityUtils.toString(entity, CHARSET);
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
            if (cert == null) {
                throw new IllegalArgumentException("Certificate InputStream not null");
            }
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
    private static class Functions {

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
         * 有限键值对
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
}
