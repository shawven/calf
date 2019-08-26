package com.test.app.support.util;

import org.apache.commons.lang3.StringUtils;

import javax.crypto.Cipher;
import java.io.ByteArrayOutputStream;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;

import static java.util.Collections.emptyMap;

/**
 * @author Shoven
 * @date 2019-07-30 14:39
 */
public class SignUtils {

    public static String toPairString(Map<String, ?> params) {
        StringBuilder content = new StringBuilder();
        List<String> keys = new ArrayList<>(params.keySet());
        Collections.sort(keys);
        int index = 0;
        for (String key : keys) {
            Object value = params.get(key);
            if (StringUtils.isNotBlank(key) && value != null) {
                content.append(index == 0 ? "" : "&").append(key).append("=").append(value);
                ++index;
            }
        }
        return content.toString();
    }

    public static Map<String, String> splitPairString(String pairString) {
        if (StringUtils.isBlank(pairString)) {
            return null;
        }
        String[] keyValuePairs = pairString.split("&");
        if (keyValuePairs.length == 0) {
            return emptyMap();
        }
        LinkedHashMap<String, String> newPairs = new LinkedHashMap<>();
        for (String keyValuePair : keyValuePairs) {
            String[] elements = StringUtils.split(keyValuePair, "=", 2);
            if (elements.length != 2) {
                continue;
            }
            newPairs.put(elements[0], elements[1]);
        }
        return newPairs;
    }

    public static String rsaSign(Map<String, ?> params, String privateKey, String charset) throws RuntimeException {
        String signContent = toPairString(params);
        return rsaSign(signContent, privateKey, charset);
    }

    public static String rsaSign(String content, String privateKey, String charset) throws RuntimeException {
        return rsaSign(content, privateKey,  "SHA1WithRSA", charset);
    }

    public static String rsa256Sign(Map<String, ?> params, String privateKey, String charset) throws RuntimeException {
        String signContent = toPairString(params);
        return rsa256Sign(signContent, privateKey, charset);
    }

    public static String rsa256Sign(String content, String privateKey, String charset) throws RuntimeException {
        return rsaSign(content, privateKey,  "SHA256WithRSA", charset);
    }

    public static String rsaSign(String content, String privateKey, String algorithm, String charset) throws RuntimeException {
        try {
            PrivateKey priKey = getPrivateKeyFromPKCS8("RSA", privateKey);
            Signature signature = Signature.getInstance(algorithm);
            signature.initSign(priKey);
            if (StringUtils.isEmpty(charset)) {
                signature.update(content.getBytes());
            } else {
                signature.update(content.getBytes(charset));
            }
            byte[] signed = signature.sign();
            return new String(Base64.getEncoder().encode(signed));
        } catch (Exception e) {
            throw new RuntimeException("RSAcontent = " + content + "; charset = " + charset, e);
        }
    }

    public static boolean rsaCheck(Map<String, ?> params, String sign, String publicKey, String charset) throws RuntimeException {
        String content = toPairString(params);
        return rsaCheck(content, sign, publicKey, charset);
    }

    public static boolean rsaCheck(String content, String sign, String publicKey, String charset) throws RuntimeException {
        return rsaCheck(content, sign, publicKey, "SHA1WithRSA", charset);
    }

    public static boolean rsa256Check(Map<String, ?> params, String sign, String publicKey, String charset) throws RuntimeException {
        String content = toPairString(params);
        return rsa256Check(content, sign, publicKey, charset);
    }

    public static boolean rsa256Check(String content, String sign, String publicKey, String charset) throws RuntimeException {
        return rsaCheck(content, sign, publicKey, "SHA256WithRSA", charset);
    }

    public static boolean rsaCheck(String content, String sign, String publicKey, String algorithm, String charset) throws RuntimeException {
        try {
            PublicKey pubKey = getPublicKeyFromX509("RSA", publicKey);
            Signature signature = Signature.getInstance(algorithm);
            signature.initVerify(pubKey);
            if (StringUtils.isEmpty(charset)) {
                signature.update(content.getBytes());
            } else {
                signature.update(content.getBytes(charset));
            }
            return signature.verify(Base64.getDecoder().decode(sign.getBytes()));
        } catch (Exception e) {
            throw new RuntimeException("RSAcontent = " + content + ",sign=" + sign + ",charset = " + charset, e);
        }
    }

    public static String rsaEncrypt(String content, String publicKey, String charset) throws RuntimeException {
        try {
            PublicKey pubKey = getPublicKeyFromX509("RSA", publicKey);
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, pubKey);
            byte[] data = StringUtils.isEmpty(charset) ? content.getBytes() : content.getBytes(charset);
            int inputLen = data.length;
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int offSet = 0;
            for (int i = 0; inputLen - offSet > 0; offSet = i * 117) {
                byte[] cache;
                if (inputLen - offSet > 117) {
                    cache = cipher.doFinal(data, offSet, 117);
                } else {
                    cache = cipher.doFinal(data, offSet, inputLen - offSet);
                }
                out.write(cache, 0, cache.length);
                ++i;
            }
            byte[] encryptedData = Base64.getEncoder().encode(out.toByteArray());
            out.close();
            return StringUtils.isEmpty(charset) ? new String(encryptedData) : new String(encryptedData, charset);
        } catch (Exception e) {
            throw new RuntimeException("EncryptContent = " + content + ",charset = " + charset, e);
        }
    }

    public static String rsaDecrypt(String content, String privateKey, String charset) throws RuntimeException {
        try {
            PrivateKey priKey = getPrivateKeyFromPKCS8("RSA", privateKey);
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, priKey);
            byte[] bytes = StringUtils.isEmpty(charset) ? content.getBytes() : content.getBytes(charset);
            byte[] encryptedData = Base64.getDecoder().decode(bytes);
            int inputLen = encryptedData.length;
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int offSet = 0;
            for(int i = 0; inputLen - offSet > 0; offSet = i * 128) {
                byte[] cache;
                if (inputLen - offSet > 128) {
                    cache = cipher.doFinal(encryptedData, offSet, 128);
                } else {
                    cache = cipher.doFinal(encryptedData, offSet, inputLen - offSet);
                }
                out.write(cache, 0, cache.length);
                ++i;
            }
            byte[] decryptedData = out.toByteArray();
            out.close();
            return StringUtils.isEmpty(charset) ? new String(decryptedData) : new String(decryptedData, charset);
        } catch (Exception e) {
            throw new RuntimeException("EncodeContent = " + content + ",charset = " + charset, e);
        }
    }

    public static PrivateKey getPrivateKeyFromPKCS8(String algorithm, String privateKey) throws Exception {
        if (StringUtils.isBlank(algorithm)) {
            throw new RuntimeException("algorithm should not be blank");
        }
        if (StringUtils.isBlank(privateKey)) {
            throw new RuntimeException("privateKey should not be blank");
        }
        KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
        byte[] encodedKey = Base64.getDecoder().decode(privateKey.getBytes());
        return keyFactory.generatePrivate(new PKCS8EncodedKeySpec(encodedKey));
    }

    public static PublicKey getPublicKeyFromX509(String algorithm, String publicKey) throws Exception {
        if (StringUtils.isBlank(algorithm)) {
            throw new RuntimeException("algorithm should not be blank");
        }
        if (StringUtils.isBlank(publicKey)) {
            throw new RuntimeException("publicKey should not be blank");
        }
        KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
        byte[] encodedKey = Base64.getDecoder().decode(publicKey.getBytes());
        return keyFactory.generatePublic(new X509EncodedKeySpec(encodedKey));
    }
}
