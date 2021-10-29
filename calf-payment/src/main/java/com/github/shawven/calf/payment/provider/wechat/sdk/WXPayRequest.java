package com.github.shawven.calf.payment.provider.wechat.sdk;

import com.github.shawven.calf.payment.support.HttpUtil;
import com.github.shawven.calf.payment.support.PaymentContextHolder;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Iterator;

public class WXPayRequest {
    private Logger logger = LoggerFactory.getLogger(WXPayRequest.class);
    private WXPayConfig config;
    private HttpUtil commonHttp;
    private HttpUtil sslHttp;

    public WXPayRequest(WXPayConfig config) {
        this.config = config;
        this.commonHttp = PaymentContextHolder.getHttp();
        this.sslHttp = HttpUtil.builder()
                .setConnectTimeout(config.getHttpConnectTimeoutMs())
                .setReadTimeout(config.getHttpReadTimeoutMs())
                .setCertificate(config.getCertStream(), config.getMchID())
                .build();
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
        HttpUtil httpUtil = useCert ? sslHttp : commonHttp;

        String url = "https://" + domain + urlSuffix;
        HttpPost httpPost = new HttpPost(url);

        RequestConfig requestConfig = RequestConfig.custom()
                .setSocketTimeout(readTimeoutMs)
                .setConnectTimeout(connectTimeoutMs)
                .build();
        httpPost.setConfig(requestConfig);

        StringEntity postEntity = new StringEntity(data, "UTF-8");
        httpPost.addHeader("Content-Type", "text/xml");
        httpPost.addHeader("User-Agent", WXPayConstants.USER_AGENT + " " + config.getMchID());
        httpPost.setEntity(postEntity);

        return httpUtil.execute(httpPost);
    }

    private String request(String urlSuffix, String uuid, String data, int connectTimeoutMs, int readTimeoutMs,
                           boolean useCert, boolean autoReport) throws Exception {
        Iterator<IWXPayDomain> iterator = config.getDomainList().iterator();

        boolean failed = true;
        Exception lastException = null;
        String response = null;

        while (failed && iterator.hasNext()) {
            IWXPayDomain next = iterator.next();
            try {
                response = request(next, urlSuffix, uuid, data, connectTimeoutMs, readTimeoutMs, useCert, autoReport);
                failed = false;
            } catch (Exception e) {
                lastException = e;
            }
        }
        if (failed && lastException != null) {
            throw lastException;
        }
        return response;
    }

    private String request(IWXPayDomain domain, String urlSuffix, String uuid, String data,
                           int connectTimeoutMs, int readTimeoutMs, boolean useCert, boolean autoReport)
            throws Exception {
        Exception exception;
        long elapsedTimeMillis = 0;
        long startTimestampMs = WXPayUtil.getCurrentTimestampMs();
        boolean firstHasDnsErr = false;
        boolean firstHasConnectTimeout = false;
        boolean firstHasReadTimeout = false;

        IWXPayDomain.DomainInfo domainInfo = domain.getDomain();
        if (domain.getDomain() == null) {
            throw new Exception("WXPayConfig.getWXPayDomain().getDomain() is empty or null");
        }
        try {
            String result = requestOnce(domainInfo.domain, urlSuffix, data, connectTimeoutMs, readTimeoutMs, useCert);
            if (autoReport) {
                elapsedTimeMillis = WXPayUtil.getCurrentTimestampMs() - startTimestampMs;
                domain.report(elapsedTimeMillis, null);
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
            domain.report(elapsedTimeMillis, exception);
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
