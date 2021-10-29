package com.github.shawven.calf.payment.properties;

import org.springframework.context.annotation.PropertySource;

/**
 * @author Shoven
 * @date 2019-09-17
 */
@PropertySource("classpath:payment.properties")
public class UnionpayProperties {

    private String mchId;

    private String encryptKey;

    private String signCertPath;

    private String encryptCertPath;

    private String rootCertPath;

    private String middleCertPath;

    private String notifyUrl;

    private String returnUrl;

    private String charset = "UTF-8";

    private String signCertPassword;

    private Boolean useSandbox;

    public String getMchId() {
        return mchId;
    }

    public void setMchId(String mchId) {
        this.mchId = mchId;
    }

    public String getEncryptKey() {
        return encryptKey;
    }

    public void setEncryptKey(String encryptKey) {
        this.encryptKey = encryptKey;
    }

    public String getSignCertPath() {
        return signCertPath;
    }

    public void setSignCertPath(String signCertPath) {
        this.signCertPath = signCertPath;
    }

    public String getEncryptCertPath() {
        return encryptCertPath;
    }

    public void setEncryptCertPath(String encryptCertPath) {
        this.encryptCertPath = encryptCertPath;
    }

    public String getRootCertPath() {
        return rootCertPath;
    }

    public void setRootCertPath(String rootCertPath) {
        this.rootCertPath = rootCertPath;
    }

    public String getMiddleCertPath() {
        return middleCertPath;
    }

    public void setMiddleCertPath(String middleCertPath) {
        this.middleCertPath = middleCertPath;
    }

    public String getSignCertPassword() {
        return signCertPassword;
    }

    public void setSignCertPassword(String signCertPassword) {
        this.signCertPassword = signCertPassword;
    }

    /**
     * 是否验证验签证书的CN，测试环境false, 正式环境true
     *
     * @return
     */
    public Boolean getValidateCnName() {
        return !getUseSandbox();
    }

    public String getNotifyUrl() {
        return notifyUrl;
    }

    public void setNotifyUrl(String notifyUrl) {
        this.notifyUrl = notifyUrl;
    }

    public String getReturnUrl() {
        return returnUrl;
    }

    public void setReturnUrl(String returnUrl) {
        this.returnUrl = returnUrl;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public Boolean getUseSandbox() {
        return useSandbox;
    }

    public void setUseSandbox(Boolean useSandbox) {
        this.useSandbox = useSandbox;
    }
}
