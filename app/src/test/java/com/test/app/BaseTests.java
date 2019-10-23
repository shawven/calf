package com.test.app;

import com.google.common.collect.ImmutableList;
import com.test.app.support.util.BigDecimals;
import com.test.app.support.util.excel.ExcelWriter;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.security.Security;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalField;
import java.util.Arrays;
import java.util.Date;

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
    public void testMain() throws Exception {
        BigDecimal.ZERO.compareTo(new BigDecimal(0).setScale(2));
    }

    @Test
    public void testClass() throws Exception {
        class A {
            private String aa;
            private String bb;

            public String getAa() {
                return aa;
            }

            public String getBb() {
                return bb;
            }

            public A(String aa, String bb) {
                this.aa = aa;
                this.bb = bb;
            }
        }
        ImmutableList<A> items = ImmutableList.of(new A("aaaa", "bbbbb"));

        new ExcelWriter<A>()
                .setData(items)
                .setHeaderName("wes")
                .setColumn("ABC", A::getAa, ExcelWriter.ColumnType.STRING)
                .writeToFile("d:/test.xlsx");
    }

    private static String byte2Hex(byte[] bytes) {
        StringBuffer stringBuffer = new StringBuffer();
        String temp = null;
        for (int i = 0; i < bytes.length; i++) {
            temp = Integer.toHexString(bytes[i] & 0xFF);
            if (temp.length() == 1) {
        //得到一位的进行补0操作
                stringBuffer.append("0");
            }
            stringBuffer.append(temp);
        }
        return stringBuffer.toString();
    }


    @Test
    public void testSpringData() throws Exception {

        System.out.println(Arrays.toString(Security.getProviders()));
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
