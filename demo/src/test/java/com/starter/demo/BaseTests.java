package com.starter.demo;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import com.starter.demo.support.util.BigDecimals;
import com.starter.demo.support.util.NodeTree;
import com.starter.demo.support.util.excel.ExcelWriter;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.util.Lists;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.security.Security;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Stream;

import static java.time.temporal.ChronoField.DAY_OF_MONTH;
import static java.util.stream.Collectors.toList;

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

        String[] codes = {"3", "", "11", "222"};
        Arrays.sort(codes);
        System.out.println(Arrays.toString(codes));
    }

    public void testTree() {
        class Menu implements NodeTree.Node<Menu> {
            String name;
            String id;

            private List<Menu> children;

            public Menu(String name, String id) {
                this.name = name;
                this.id = id;
            }

            @Override
            public List<Menu> getChildren() {
                return children;
            }

            @Override
            public void setChildren(List<Menu> children) {
                this.children = children;
            }
        }


        ArrayList<Menu> menus = Lists.newArrayList(
                new Menu("A", "1"),
                new Menu("AA", "11"),
                new Menu("AAA", "111"),
                new Menu("B", "2"));

        List<Menu> tree = NodeTree.<Menu, Menu>from(menus)
                .rootFilter(menu -> menu.id.length() == 1)
                .childFilter((parent, child) -> parent.id.concat("1").equals(child.id))
                .map(menu -> menu)
                .build();

        System.out.println(tree);
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
