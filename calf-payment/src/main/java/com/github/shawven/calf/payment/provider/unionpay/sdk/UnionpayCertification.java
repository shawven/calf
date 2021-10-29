package com.github.shawven.calf.payment.provider.unionpay.sdk;

import com.github.shawven.calf.payment.support.CertUtils;
import com.github.shawven.calf.payment.support.PaymentUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.*;
import java.security.cert.*;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

public class UnionpayCertification {

    private Logger logger = LoggerFactory.getLogger(UnionpayCertification.class);

    /**
     * 证书容器，存储对商户请求报文签名私钥证书.
     */
    private KeyStore keyStore = null;
    /**
     * 敏感信息加密公钥证书
     */
    private X509Certificate encryptCert = null;
    /**
     * 验签中级证书
     */
    private X509Certificate middleCert = null;
    /**
     * 验签根证书
     */
    private X509Certificate rootCert = null;

    private String signCertPath;

    private String encryptCertPath;

    private String rootCertPath;

    private String middleCertPath;

    private String signPassword;

    private boolean validateCnName;

    public UnionpayCertification(String signCertPath, String encryptCertPath, String rootCertPath, String middleCertPath,
                                 String signPassword, boolean validateCnName) {
        this.signCertPath = signCertPath;
        this.encryptCertPath = encryptCertPath;
        this.rootCertPath = rootCertPath;
        this.middleCertPath = middleCertPath;
        this.signPassword = signPassword;
        this.validateCnName = validateCnName;
        this.init();
    }

    /**
     * 初始化所有证书.
     */
    private void init() {
        try {
            //向系统添加BC provider
            Security.addProvider(new BouncyCastleProvider());

            //初始化签名私钥证书
            if (PaymentUtils.isBlankString(signCertPath)) {
                throw new UnionpayException("签名证书路径为空");
            }
            if (signPassword == null) {
                throw new UnionpayException("签名证书密码为空");
            }
            keyStore = CertUtils.getKeyStore(signCertPath, signPassword);

            //初始化验签证书的中级证书
            if (PaymentUtils.isBlankString(middleCertPath)) {
                throw new UnionpayException("银联中级证书路径为空");
            }
            middleCert = CertUtils.getCertificate(middleCertPath);

            //初始化验签证书的根证书
            if (PaymentUtils.isBlankString(rootCertPath)) {
                throw new UnionpayException("银联根证书路径为空");
            }
            rootCert = CertUtils.getCertificate(rootCertPath);

            //初始化加密公钥
            if (PaymentUtils.isBlankString(encryptCertPath)) {
                throw new UnionpayException("敏感信息加密证书路径为空");
            }
            encryptCert = CertUtils.getCertificate(encryptCertPath);
        } catch (Exception e) {
            throw new RuntimeException("初始化证书失败：" + e.getMessage(), e);
        }
    }

    /**
     * 获取签名证书的certId
     *
     * @return 证书的物理编号
     */
    public String getSignCertId() throws UnionpayException {
        try {
            X509Certificate cert;
            Enumeration<String> aliases = keyStore.aliases();
            String keyAlias = null;
            if (aliases.hasMoreElements()) {
                keyAlias = aliases.nextElement();
            }
            cert = (X509Certificate) keyStore.getCertificate(keyAlias);
            return cert.getSerialNumber().toString();
        } catch (KeyStoreException e) {
            throw new UnionpayException("获取签名证书certId错误", e);
        }
    }

    /**
     * 获取签名证书的私钥
     *
     * @return
     */
    public PrivateKey getSignCertPrivateKey() throws UnionpayException {
        try {
            Enumeration<String> aliasenum = keyStore.aliases();
            String keyAlias = null;
            if (aliasenum.hasMoreElements()) {
                keyAlias = aliasenum.nextElement();
            }
            return (PrivateKey) keyStore.getKey(keyAlias, signPassword.toCharArray());
        } catch (KeyStoreException | UnrecoverableKeyException | NoSuchAlgorithmException e) {
            throw new UnionpayException("获取签名证书PrivateKey错误", e);
        }
    }

    /**
     * 获取敏感信息加密证书的certId
     *
     * @return
     */
    public String getEncryptCertId() {
        return encryptCert.getSerialNumber().toString();
    }

    /**
     * 获取敏感信息加密证书PublicKey
     *
     * @return
     */
    public PublicKey getEncryptCertPublicKey() {
        return encryptCert.getPublicKey();
    }

    /**
     * 获取验签公钥使用的中级证书
     *
     * @return
     */
    public X509Certificate getMiddleCert() {
        return middleCert;
    }

    /**
     * 验签公钥使用的根证书
     *
     * @return
     */
    public X509Certificate getRootCert() {
        return rootCert;
    }

    /**
     * 检查证书链
     *
     * @param cert 待验证的证书
     * @return
     */
    public boolean verifyCertificate(X509Certificate cert) {
        if (cert == null) {
            return false;
        }
        //验证有效期
        try {
            cert.checkValidity();
        } catch (CertificateExpiredException e) {
            logger.error("证书已经过期");
            return false;
        } catch (CertificateNotYetValidException e) {
            logger.error("证书未激活");
            return false;
        }
        try {
            cert.verify(middleCert.getPublicKey());
        } catch (Exception e) {
            logger.error("验证证书错误", e);
            return false;
        }
        if (!verifyCertificateChain(cert)) {
            return false;
        }
        // 验证公钥是否属于银联
        if (validateCnName) {
            return UnionpayConstants.UNIONPAY_CNNAME.equals(getIdentitiesFromCertificate(cert));
        } else {
            return "00040000:SIGN".equals(getIdentitiesFromCertificate(cert));
        }
    }

    /**
     * 验证书链。
     *
     * @param cert
     * @return
     */
    private boolean verifyCertificateChain(X509Certificate cert) {
        if (cert == null) {
            return false;
        }
        X509Certificate middleCert = getMiddleCert();
        if (middleCert == null) {
            return false;
        }
        X509Certificate rootCert = getRootCert();
        if (rootCert == null) {
            return false;
        }

        try {
            Set<X509Certificate> intermediateCerts = new HashSet<>();
            intermediateCerts.add(rootCert);
            intermediateCerts.add(middleCert);
            intermediateCerts.add(cert);

            CertStore intermediateCertStore = CertStore.getInstance("Collection",
                    new CollectionCertStoreParameters(intermediateCerts), "BC");

            X509CertSelector selector = new X509CertSelector();
            selector.setCertificate(cert);
            Set<TrustAnchor> trustAnchors = new HashSet<>();
            trustAnchors.add(new TrustAnchor(rootCert, null));

            PKIXBuilderParameters pkixParams = new PKIXBuilderParameters(trustAnchors, selector);
            pkixParams.setRevocationEnabled(false);
            pkixParams.addCertStore(intermediateCertStore);

            CertPathBuilder builder = CertPathBuilder.getInstance("PKIX", "BC");
            PKIXCertPathBuilderResult result = (PKIXCertPathBuilderResult) builder
                    .build(pkixParams);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取证书的CN
     *
     * @param aCert
     * @return
     */
    private String getIdentitiesFromCertificate(X509Certificate aCert) {
        String tDN = aCert.getSubjectDN().toString();
        String tPart = "";
        if ((tDN != null)) {
            String[] tSplitStr = tDN.substring(tDN.indexOf("CN=")).split("@");
            if (tSplitStr.length > 2 && tSplitStr[2] != null) {
                tPart = tSplitStr[2];
            }
        }
        return tPart;
    }

}
