package com.test.payment.support;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_8;


/**
 * HttpClient工具类
 *
 * @author Shoven
 * @date 2019-08-26
 */
public class PaymentHttpUtils {

    private static final Charset CHARSET = UTF_8;

    /**
     * 普通GET请求
     *
     * @param url
     * @param query
     * @return
     * @throws IOException
     */
    public static String get(String url, Map<String, ?> query) throws IOException {
        HttpGet httpGet = new HttpGet(Functions.joinUrlAndParams(url, query));
        return HttpExecutor.execute(httpGet);
    }

    /**
     * 普通GET请求
     *
     * @param url
     * @param query
     * @param connectTimeOut
     * @param readTimeout
     * @return
     * @throws IOException
     */
    public static String get(String url, Map<String, ?> query, int connectTimeOut, int readTimeout) throws IOException {
        HttpGet httpGet = new HttpGet(Functions.joinUrlAndParams(url, query));
        return HttpExecutor.execute(httpGet, connectTimeOut, readTimeout);
    }

    /**
     * 普通表单提交POST请求
     *
     * @param url
     * @param payload
     * @return
     * @throws IOException
     */
    public static String post(String url, Map<String, ?> payload) throws IOException {
        HttpPost httpPost = new HttpPost(url);
        httpPost.setEntity(Functions.buildUrlEncodedBody(payload));
        return HttpExecutor.execute(httpPost);
    }

    /**
     * 普通表单提交POST请求
     *
     * @param url
     * @param payload
     * @param connectTimeOut
     * @param readTimeout
     * @return
     * @throws IOException
     */
    public static String post(String url, Map<String, ?> payload, int connectTimeOut, int readTimeout) throws IOException {
        HttpPost httpPost = new HttpPost(url);
        httpPost.setEntity(Functions.buildUrlEncodedBody(payload));
        return HttpExecutor.execute(httpPost, connectTimeOut, readTimeout);
    }

    /**
     * Http执行器
     */
    private static class HttpExecutor {

        private static final int CONNECT_TIMEOUT = 2;

        private static final int SOCKET_TIMEOUT = 5;

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

        private static String execute(HttpRequestBase request, int connectTimeOut, int readTimeout) throws IOException {
            RequestConfig.Builder builder = RequestConfig.copy(HttpExecutor.REQUEST_CONFIG);
            builder.setConnectTimeout(connectTimeOut);
            builder.setSocketTimeout(readTimeout);
            request.setConfig(builder.build());
            return execute(request);
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
        private static String joinUrlAndParams(String url, Map<String, ?> payload) {
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
        private static StringEntity buildUrlEncodedBody(Map<String, ?> payload) {
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

        private static boolean isValidPair(String key, Object value) {
            return !isBlankString(key) && value != null && !isBlankString(value.toString());
        }

        private static boolean isBlankString(CharSequence cs) {
            int strLen;
            if (cs != null && (strLen = cs.length()) != 0) {
                for(int i = 0; i < strLen; ++i) {
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
}
