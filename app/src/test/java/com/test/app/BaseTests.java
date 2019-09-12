package com.test.app;

import com.alipay.api.internal.util.AlipaySignature;
import com.test.app.support.util.HttpClientUtils;
import com.test.app.support.util.SignUtils;
import com.test.app.support.util.excel.ExcelReader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @author Shoven
 * @date 2019-07-30 15:37
 */
public class BaseTests {

    private long startAt;

    @Before
    public void start() {
        startAt = System.currentTimeMillis();
    }

    @After
    public void end() {
        System.out.println("usage: " + (System.currentTimeMillis() - startAt) + " ms");
        startAt = 0;
    }

    @Test
    public void main() throws IOException {
        System.out.println(HttpClientUtils.get("https://www.baidu.com"));


    }
    @Test
    public void testSpringData() throws Exception {

        String s1 = "MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQDGKo451nNnFAan1AvJfYhtUgtDjR69faHTJUBO1u99WMackTAtmgDzMJdcpkh4h4DYDyhumLR1vA1d495zNJ3sSR450VEyTgQnjlSd8P0slX1qVVpxZISA//flV/SDWufrL1KRrRXWGh3n7bm9gmMcjqGAJ6c8KankoSBSg7GdvL9OTIyM6bOjxlkOTGnPcjq4+6362R7z3je1AU39BV1YHU8hwytHRvBa0lHv9Xl0+kypX0YlMQpL8IbKnZ+BbK5gTMvAMMF4mwSIw/OnK366/tNb5s1MGZg1WSw5ux3H1TL278m6lodsnEHNSgB9iEH1P3HcXFYGYucg6FZNza17AgMBAAECggEAQM7ADshEm4xgFNjzNeUZDe+MAX6QdBY3Ky9+lYoTbqHu6ltNL0yzTNUyWzmCb/CYwZBLf8fvFKX4lSElqfWmRBUxjmUXMeVz1F/m6nRayA7nojPhzT27W9jagKXAMIk1WaPzU/MQNiH6b9Q+L20J7tfyl0/gkHtrtXrAcFvjZZs12CmeFEpDzrPJv8e/Qmx3ZSt9ciEuB4WTI9S1zatg58DUB2uyAXDvfEz0LNCtwoc/xPRrRbPIWQnU+pKvFmiGVDLbh/x4A4vHjLaO8VPWvmjHpTHgNjvZdyx5nTra5nsJVNGOqDgT67+Hv2E+QD1uCphKha7zzuYpKsUzrC3BoQKBgQD3cB6GI9Lv01tXfL9VVpXpeKM+RgX92FkOYhWC6OupgeJNBw3CJ0NfPSVGFsZSfiFy3nzGztQpopeTF3DbKaO873w4duojMdGinb5HQyIW+apFIRiy9Vvrfua5MfdfAtF6Q0F8tGO12curGIDzEuhGVPiZfgN+pzf82OEr+n3NEQKBgQDNBfjrEkMvKDNjd+amJ0j2x0S7pvAm3kBLkzTfS6aT0ybuwHMacm36hUlg0nkp3T0VOXMvzKtfz0SliEBfF0FLxyg5n300CF5lrLIwBGbzjx9hHeTW+/PMIzk9OalxSJezTgl9OMLviBhwZvgRtezK7eXqp8lHMfKAfg8jhk8BywKBgATPXyiIZb9XwnzO5gFIr51cRQAe6Fro68JF0dk7oeknpziMHI5bqQme7KySIMTVwtyyyoYq2yD8Eio/2GfKRW6U0TtfxGluvH6Gxn4oJe+AZvj1Db/c2S6Yxu9uszpbmargx9MexplwZG8tCMSw52cGm9aQBo1dvs7hgG8UbYGhAoGAYAWaDU6iVKW5W66+QbdIfvvY/yIye6G/7yVkr+gYfkd4hn1v60UIqTpfoY8x3B+YCia8AfkTkm3assRbka2skTcXadV+qC37OBdn5QFKzmuLwMplcSLTjho5Jqmo6DudgQbdft5kvha5i3tOV80it/AvrHUk2clU0EU6BeNTgX0CgYA4FrJnHMF0QmsZ0lS9noAJSTjd0lB3jO/ZEkAcPifx30hI8iphRX2fiYOCwQaiHM11hfeIA7vdK//6gb6Dq8F2AztMpP8Avpnv/hPr/lVSQsyHJ3sGYLE8M8Nycvb57pwSF7B0KINRae/gQEhrl7ythahscF+zS6/gbXV1E41kFA==";
        String s2 = "MIIEogIBAAKCAQEAxiqOOdZzZxQGp9QLyX2IbVILQ40evX2h0yVATtbvfVjGnJEwLZoA8zCXXKZIeIeA2A8obpi0dbwNXePeczSd7EkeOdFRMk4EJ45UnfD9LJV9alVacWSEgP/35Vf0g1rn6y9Ska0V1hod5+25vYJjHI6hgCenPCmp5KEgUoOxnby/TkyMjOmzo8ZZDkxpz3I6uPut+tke8943tQFN/QVdWB1PIcMrR0bwWtJR7/V5dPpMqV9GJTEKS/CGyp2fgWyuYEzLwDDBeJsEiMPzpyt+uv7TW+bNTBmYNVksObsdx9Uy9u/JupaHbJxBzUoAfYhB9T9x3FxWBmLnIOhWTc2tewIDAQABAoIBAEDOwA7IRJuMYBTY8zXlGQ3vjAF+kHQWNysvfpWKE26h7upbTS9Ms0zVMls5gm/wmMGQS3/H7xSl+JUhJan1pkQVMY5lFzHlc9Rf5up0WsgO56Iz4c09u1vY2oClwDCJNVmj81PzEDYh+m/UPi9tCe7X8pdP4JB7a7V6wHBb42WbNdgpnhRKQ86zyb/Hv0Jsd2UrfXIhLgeFkyPUtc2rYOfA1AdrsgFw73xM9CzQrcKHP8T0a0WzyFkJ1PqSrxZohlQy24f8eAOLx4y2jvFT1r5ox6Ux4DY72XcseZ062uZ7CVTRjqg4E+u/h79hPkA9bgqYSoWu887mKSrFM6wtwaECgYEA93AehiPS79NbV3y/VVaV6XijPkYF/dhZDmIVgujrqYHiTQcNwidDXz0lRhbGUn4hct58xs7UKaKXkxdw2ymjvO98OHbqIzHRop2+R0MiFvmqRSEYsvVb637muTH3XwLRekNBfLRjtdnLqxiA8xLoRlT4mX4Dfqc3/NjhK/p9zRECgYEAzQX46xJDLygzY3fmpidI9sdEu6bwJt5AS5M030umk9Mm7sBzGnJt+oVJYNJ5Kd09FTlzL8yrX89EpYhAXxdBS8coOZ99NAheZayyMARm848fYR3k1vvzzCM5PTmpcUiXs04JfTjC74gYcGb4EbXsyu3l6qfJRzHygH4PI4ZPAcsCgYAEz18oiGW/V8J8zuYBSK+dXEUAHuha6OvCRdHZO6HpJ6c4jByOW6kJnuyskiDE1cLcssqGKtsg/BIqP9hnykVulNE7X8Rpbrx+hsZ+KCXvgGb49Q2/3NkumMbvbrM6W5mq4MfTHsaZcGRvLQjEsOdnBpvWkAaNXb7O4YBvFG2BoQKBgGAFmg1OolSluVuuvkG3SH772P8iMnuhv+8lZK/oGH5HeIZ9b+tFCKk6X6GPMdwfmAomvAH5E5Jt2rLEW5GtrJE3F2nVfqgt+zgXZ+UBSs5ri8DKZXEi044aOSapqOg7nYEG3X7eZL4WuYt7TlfNIrfwL6x1JNnJVNBFOgXjU4F9AoGAOBayZxzBdEJrGdJUvZ6ACUk43dJQd4zv2RJAHD4n8d9ISPIqYUV9n4mDgsEGohzNdYX3iAO73Sv/+oG+g6vBdgM7TKT/AL6Z7/4T6/5VUkLMhyd7BmCxPDPDcnL2+e6cEhewdCiDUWnv4EBIa5e8rYWobHBfs0uv4G11dRONZBQ=";


        SignUtils.getPrivateKeyFromPKCS8("RSA", s1);
//        SignUtils.getPrivateKeyFromPKCS8("RSA", s2);
    }

    private Class class1() {
        return ApplicationTests.class;
    }
    private Class<?> class2() {
        return ApplicationTests.class;
    }
    private Class<ApplicationTests> class3() {
        return ApplicationTests.class;
    }

}
