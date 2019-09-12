package com.test.payment.supplier.wechat.sdk;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.security.KeyStore;
import java.security.SecureRandom;

import static com.test.payment.supplier.wechat.sdk.WXPayConstants.USER_AGENT;


public class WXPayRequest {
    private Logger logger = LoggerFactory.getLogger(WXPayRequest.class);
    private WXPayConfig config;

    public WXPayRequest(WXPayConfig config) {
        this.config = config;
    }

    /**
     * 请求，只请求一次，不做重试
     *
     * @param domain
     * @param urlSuffix
     * @param data
     * @param connectTimeoutMs
     * @param readTimeoutMs
     * @param useCert          是否使用证书，针对退款、撤销等操作
     * @return
     * @throws Exception
     */
    private String requestOnce(final String domain, String urlSuffix, String data, int connectTimeoutMs, int readTimeoutMs, boolean useCert) throws Exception {
        BasicHttpClientConnectionManager connManager;
        if (useCert) {
            // 证书
            char[] password = config.getMchID().toCharArray();
            InputStream certStream = config.getCertStream();
            KeyStore ks = KeyStore.getInstance("PKCS12");
            ks.load(certStream, password);

            // 实例化密钥库 & 初始化密钥工厂
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(ks, password);

            // 创建 SSLContext
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(kmf.getKeyManagers(), null, new SecureRandom());

            SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(
                    sslContext,
                    new String[]{"TLSv1"},
                    null,
                    new DefaultHostnameVerifier());

            connManager = new BasicHttpClientConnectionManager(
                    RegistryBuilder.<ConnectionSocketFactory>create()
                            .register("http", PlainConnectionSocketFactory.getSocketFactory())
                            .register("https", sslConnectionSocketFactory)
                            .build(),
                    null,
                    null,
                    null
            );
        } else {
            connManager = new BasicHttpClientConnectionManager(
                    RegistryBuilder.<ConnectionSocketFactory>create()
                            .register("http", PlainConnectionSocketFactory.getSocketFactory())
                            .register("https", SSLConnectionSocketFactory.getSocketFactory())
                            .build(),
                    null,
                    null,
                    null
            );
        }

        HttpClient httpClient = HttpClientBuilder.create()
                .setConnectionManager(connManager)
                .build();

        String url = "https://" + domain + urlSuffix;
        HttpPost httpPost = new HttpPost(url);

        RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(readTimeoutMs).setConnectTimeout(connectTimeoutMs).build();
        httpPost.setConfig(requestConfig);

        StringEntity postEntity = new StringEntity(data, "UTF-8");
        httpPost.addHeader("Content-Type", "text/xml");
        httpPost.addHeader("User-Agent", USER_AGENT + " " + config.getMchID());
        httpPost.setEntity(postEntity);

        HttpResponse httpResponse = httpClient.execute(httpPost);
        HttpEntity httpEntity = httpResponse.getEntity();
        return EntityUtils.toString(httpEntity, "UTF-8");

    }


    private String request(String urlSuffix, String uuid, String data, int connectTimeoutMs, int readTimeoutMs, boolean useCert, boolean autoReport) throws Exception {
        Exception exception;
        long elapsedTimeMillis = 0;
        long startTimestampMs = WXPayUtil.getCurrentTimestampMs();
        boolean firstHasDnsErr = false;
        boolean firstHasConnectTimeout = false;
        boolean firstHasReadTimeout = false;
        IWXPayDomain.DomainInfo domainInfo = config.getWXPayDomain().getDomain();
        if (domainInfo == null) {
            throw new Exception("WXPayConfig.getWXPayDomain().getDomain() is empty or null");
        }
        try {
            String result = requestOnce(domainInfo.domain, urlSuffix, data, connectTimeoutMs, readTimeoutMs, useCert);
            if (autoReport) {
                elapsedTimeMillis = WXPayUtil.getCurrentTimestampMs() - startTimestampMs;
                config.getWXPayDomain().report(elapsedTimeMillis, null);
                WXPayReport.getInstance(config).report(
                        uuid,
                        elapsedTimeMillis,
                        domainInfo.domain,
                        domainInfo.primaryDomain,
                        connectTimeoutMs,
                        readTimeoutMs,
                        firstHasDnsErr,
                        firstHasConnectTimeout,
                        firstHasReadTimeout);
            }
            return result;
        } catch (UnknownHostException ex) {  // dns 解析错误，或域名不存在
            exception = ex;
            logger.warn("UnknownHostException for domainInfo {}", domainInfo);
            if (autoReport) {

                firstHasDnsErr = true;
                elapsedTimeMillis = WXPayUtil.getCurrentTimestampMs() - startTimestampMs;
                WXPayReport.getInstance(config).report(
                        uuid,
                        elapsedTimeMillis,
                        domainInfo.domain,
                        domainInfo.primaryDomain,
                        connectTimeoutMs,
                        readTimeoutMs,
                        firstHasDnsErr,
                        firstHasConnectTimeout,
                        firstHasReadTimeout
                );
            }

        } catch (ConnectTimeoutException ex) {
            exception = ex;
            logger.warn("connect timeout happened for domainInfo {}", domainInfo);
            if (autoReport) {
                firstHasConnectTimeout = true;
                elapsedTimeMillis = WXPayUtil.getCurrentTimestampMs() - startTimestampMs;
                WXPayReport.getInstance(config).report(
                        uuid,
                        elapsedTimeMillis,
                        domainInfo.domain,
                        domainInfo.primaryDomain,
                        connectTimeoutMs,
                        readTimeoutMs,
                        firstHasDnsErr,
                        firstHasConnectTimeout,
                        firstHasReadTimeout
                );
            }
        } catch (SocketTimeoutException ex) {
            exception = ex;
            logger.warn("timeout happened for domainInfo {}", domainInfo);
            if (autoReport) {
                firstHasReadTimeout = true;
                elapsedTimeMillis = WXPayUtil.getCurrentTimestampMs() - startTimestampMs;
                WXPayReport.getInstance(config).report(
                        uuid,
                        elapsedTimeMillis,
                        domainInfo.domain,
                        domainInfo.primaryDomain,
                        connectTimeoutMs,
                        readTimeoutMs,
                        firstHasDnsErr,
                        firstHasConnectTimeout,
                        firstHasReadTimeout);
            }
        } catch (Exception ex) {
            exception = ex;
            if (autoReport) {
                elapsedTimeMillis = WXPayUtil.getCurrentTimestampMs() - startTimestampMs;
                WXPayReport.getInstance(config).report(
                        uuid,
                        elapsedTimeMillis,
                        domainInfo.domain,
                        domainInfo.primaryDomain,
                        connectTimeoutMs,
                        readTimeoutMs,
                        firstHasDnsErr,
                        firstHasConnectTimeout,
                        firstHasReadTimeout);
            }

        }
        if (autoReport) {
            config.getWXPayDomain().report(elapsedTimeMillis, exception);
        }
        throw exception;
    }


    /**
     * 可重试的，非双向认证的请求
     *
     * @param urlSuffix
     * @param uuid
     * @param data
     * @return
     */
    public String requestWithoutCert(String urlSuffix, String uuid, String data, boolean autoReport) throws Exception {
        return this.request(urlSuffix, uuid, data, config.getHttpConnectTimeoutMs(), config.getHttpReadTimeoutMs(), false, autoReport);
    }

    /**
     * 可重试的，非双向认证的请求
     *
     * @param urlSuffix
     * @param uuid
     * @param data
     * @param connectTimeoutMs
     * @param readTimeoutMs
     * @return
     */
    public String requestWithoutCert(String urlSuffix, String uuid, String data, int connectTimeoutMs, int readTimeoutMs, boolean autoReport) throws Exception {
        return this.request(urlSuffix, uuid, data, connectTimeoutMs, readTimeoutMs, false, autoReport);
    }

    /**
     * 可重试的，双向认证的请求
     *
     * @param urlSuffix
     * @param uuid
     * @param data
     * @return
     */
    public String requestWithCert(String urlSuffix, String uuid, String data, boolean autoReport) throws Exception {
        return this.request(urlSuffix, uuid, data, config.getHttpConnectTimeoutMs(), config.getHttpReadTimeoutMs(), true, autoReport);
    }

    /**
     * 可重试的，双向认证的请求
     *
     * @param urlSuffix
     * @param uuid
     * @param data
     * @param connectTimeoutMs
     * @param readTimeoutMs
     * @return
     */
    public String requestWithCert(String urlSuffix, String uuid, String data, int connectTimeoutMs, int readTimeoutMs, boolean autoReport) throws Exception {
        return this.request(urlSuffix, uuid, data, connectTimeoutMs, readTimeoutMs, true, autoReport);
    }
}
