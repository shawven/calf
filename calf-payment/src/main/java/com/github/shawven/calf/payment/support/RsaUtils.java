package com.github.shawven.calf.payment.support;


import javax.crypto.Cipher;
import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * @author Shoven
 * @date 2019-07-30 14:39
 */
public class RsaUtils {

    public static String rsaSign(String content, String privateKey, Charset charset) throws RuntimeException {
        PrivateKey priKey;
        try {
            priKey = getPrivateKeyFromPKCS8("RSA", privateKey);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return rsaSign(content, priKey, charset);
    }

    public static String rsaSign(String content, PrivateKey privateKey, Charset charset) throws RuntimeException {
        return sign(content, privateKey, "SHA1WithRSA", charset);
    }

    public static String rsa2Sign(String content, String privateKey, Charset charset) throws RuntimeException {
        PrivateKey priKey;
        try {
            priKey = getPrivateKeyFromPKCS8("RSA", privateKey);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return rsa2Sign(content, priKey, charset);
    }

    public static String rsa2Sign(String content, PrivateKey privateKey, Charset charset) throws RuntimeException {
        return sign(content, privateKey, "SHA256WithRSA", charset);
    }

    public static String sign(String content, String privateKey, String algorithm, Charset charset) throws RuntimeException {
        PrivateKey priKey;
        try {
            priKey = getPrivateKeyFromPKCS8("RSA", privateKey);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return sign(content, priKey, algorithm, charset);
    }

    public static String sign(String content, PrivateKey privateKey, String algorithm, Charset charset) throws RuntimeException {
        try {
            Signature signature = Signature.getInstance(algorithm);
            signature.initSign(privateKey);
            if (charset == null) {
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

    public static boolean rsaVerify(String content, String sign, String publicKey, Charset charset) throws RuntimeException {
        PublicKey pubKey;
        try {
            pubKey = getPublicKeyFromX509("RSA", publicKey);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return rsaVerify(content, sign, pubKey, charset);
    }

    public static boolean rsaVerify(String content, String sign, PublicKey publicKey, Charset charset) throws RuntimeException {
        return verify(content, sign, publicKey, "SHA1WithRSA", charset);
    }

    public static boolean rsa2Verify(String content, String sign, String publicKey, Charset charset) throws RuntimeException {
        PublicKey pubKey;
        try {
            pubKey = getPublicKeyFromX509("RSA", publicKey);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return rsa2Verify(content, sign, pubKey, charset);
    }

    public static boolean rsa2Verify(String content, String sign, PublicKey publicKey, Charset charset) throws RuntimeException {
        return verify(content, sign, publicKey, "SHA256WithRSA", charset);
    }

    public static boolean verify(String content, String sign, String publicKey, String algorithm, Charset charset) throws RuntimeException {
        PublicKey pubKey;
        try {
            pubKey = getPublicKeyFromX509("RSA", publicKey);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return verify(content, sign, pubKey, algorithm, charset);
    }

    public static boolean verify(String content, String sign, PublicKey publicKey, String algorithm, Charset charset) throws RuntimeException {
        try {
            Signature signature = Signature.getInstance(algorithm);
            signature.initVerify(publicKey);
            if (charset == null) {
                signature.update(content.getBytes());
            } else {
                signature.update(content.getBytes(charset));
            }
            return signature.verify(Base64.getDecoder().decode(sign.getBytes()));
        } catch (Exception e) {
            throw new RuntimeException("RSAcontent = " + content + ",sign=" + sign + ",charset = " + charset, e);
        }
    }

    public static String encrypt(String content, String publicKey) throws RuntimeException {
        PublicKey pubKey;
        try {
            pubKey = getPublicKeyFromX509("RSA", publicKey);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return encrypt(content, pubKey, null);
    }

    public static String encrypt(String content, PublicKey publicKey) throws RuntimeException {
        return encrypt(content, publicKey, null);
    }

    public static String encrypt(String content, String publicKey, Charset charset) throws RuntimeException {
        PublicKey pubKey;
        try {
            pubKey = getPublicKeyFromX509("RSA", publicKey);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return encrypt(content, pubKey, charset);
    }

    public static String encrypt(String content, PublicKey publicKey, Charset charset) throws RuntimeException {
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] data = charset == null ? content.getBytes() : content.getBytes(charset);
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
            return charset == null ? new String(encryptedData) : new String(encryptedData, charset);
        } catch (Exception e) {
            throw new RuntimeException("EncryptContent = " + content + ",charset = " + charset, e);
        }
    }

    public static String decrypt(String content, String privateKey) throws RuntimeException {
        PrivateKey priKey;
        try {
            priKey = getPrivateKeyFromPKCS8("RSA", privateKey);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return decrypt(content, priKey, null);
    }

    public static String decrypt(String content, PrivateKey privateKey) throws RuntimeException {
        return decrypt(content, privateKey, null);
    }

    public static String decrypt(String content, String privateKey, Charset charset) throws RuntimeException {
        PrivateKey priKey;
        try {
            priKey = getPrivateKeyFromPKCS8("RSA", privateKey);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return decrypt(content, priKey, charset);
    }

    public static String decrypt(String content, PrivateKey privateKey, Charset charset) throws RuntimeException {
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] bytes = charset == null ? content.getBytes() : content.getBytes(charset);
            byte[] encryptedData = Base64.getDecoder().decode(bytes);
            int inputLen = encryptedData.length;
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int offSet = 0;
            for (int i = 0; inputLen - offSet > 0; offSet = i * 128) {
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
            return charset == null ? new String(decryptedData) : new String(decryptedData, charset);
        } catch (Exception e) {
            throw new RuntimeException("EncodeContent = " + content + ",charset = " + charset, e);
        }
    }

    public static PrivateKey getPrivateKeyFromPKCS8(String algorithm, String privateKey) throws Exception {
        if (isBlankString(algorithm)) {
            throw new RuntimeException("algorithm should not be blank");
        }
        if (isBlankString(privateKey)) {
            throw new RuntimeException("privateKey should not be blank");
        }
        KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
        byte[] encodedKey = Base64.getDecoder().decode(privateKey.getBytes());
        return keyFactory.generatePrivate(new PKCS8EncodedKeySpec(encodedKey));
    }

    public static PublicKey getPublicKeyFromX509(String algorithm, String publicKey) throws Exception {
        if (isBlankString(algorithm)) {
            throw new RuntimeException("algorithm should not be blank");
        }
        if (isBlankString(publicKey)) {
            throw new RuntimeException("publicKey should not be blank");
        }
        KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
        byte[] encodedKey = Base64.getDecoder().decode(publicKey.getBytes());
        return keyFactory.generatePublic(new X509EncodedKeySpec(encodedKey));
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
