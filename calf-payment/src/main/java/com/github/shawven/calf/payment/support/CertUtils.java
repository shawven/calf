package com.github.shawven.calf.payment.support;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Enumeration;

/**
 * @author Shoven
 * @date 2019-09-19
 */
public class CertUtils {

    public static PrivateKey getPrivateKeyByPfx(String pfxPath, String password) throws Exception {
        try (FileInputStream fis = new FileInputStream(pfxPath)) {
            return getPrivateKeyByPfx(PaymentUtils.readBytes(fis), password);
        }
    }

    public static PrivateKey getPrivateKeyByPfx(byte[] pfxData, String password) throws Exception {
        PrivateKey privateKey = null;
        KeyStore keystore = getKeyStore(pfxData, password);
        Enumeration<String> enums = keystore.aliases();
        String keyAlias;
        while (enums.hasMoreElements()) {
            keyAlias = enums.nextElement();
            if (keystore.isKeyEntry(keyAlias)) {
                privateKey = (PrivateKey) keystore.getKey(keyAlias, password.toCharArray());
            }
        }
        return privateKey;
    }

    public static X509Certificate getCertificateByPfx(String pfxPath, String password) throws Exception {
        try (FileInputStream fis = new FileInputStream(pfxPath)) {
            return getCertificateByPfx(PaymentUtils.readBytes(fis), password);
        }
    }

    public static X509Certificate getCertificateByPfx(byte[] pfxData, String password) throws Exception {
        X509Certificate x509Certificate = null;
        KeyStore keystore = getKeyStore(pfxData, password);
        Enumeration<String> enums = keystore.aliases();
        String keyAlias = "";
        while (enums.hasMoreElements()) {
            keyAlias = enums.nextElement();
            if (keystore.isKeyEntry(keyAlias)) {
                x509Certificate = (X509Certificate) keystore.getCertificate(keyAlias);
            }
        }
        return x509Certificate;
    }

    public static X509Certificate getCertificate(String certPath) throws Exception {
        try (InputStream inputStream = new FileInputStream(certPath)) {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            return (X509Certificate) cf.generateCertificate(inputStream);
        } catch (IOException | CertificateException e) {
            throw new Exception(e);
        }
    }

    public static X509Certificate getCertificate(byte[] certData) throws Exception {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        return (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(certData));

    }

    public static KeyStore getKeyStore(String pfxPath, String password) throws Exception {
        try (FileInputStream fis = new FileInputStream(pfxPath)) {
            return getKeyStore(PaymentUtils.readBytes(fis), password);
        }
    }

    public static KeyStore getKeyStore(byte[] data, String password) throws Exception {
        KeyStore keystore = KeyStore.getInstance("PKCS12");
        keystore.load(new ByteArrayInputStream(data), password.toCharArray());
        return keystore;
    }
}
