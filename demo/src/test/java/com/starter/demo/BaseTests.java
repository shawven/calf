package com.starter.demo;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import com.starter.demo.support.util.BigDecimals;
import com.starter.demo.support.util.NodeTree;
import com.starter.demo.support.util.excel.ExcelWriter;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.http.util.Asserts;
import org.assertj.core.util.Lists;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.data.util.Pair;

import javax.sound.midi.Soundbank;
import java.math.BigDecimal;
import java.security.Security;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.time.temporal.ChronoField.DAY_OF_MONTH;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

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
    public void test() {

        System.out.println(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
    }


    @Test
    public void testParallelStreamOrdered() {
        class A {
            private String aa;
            private String bb;

            public String getAa() {
                return aa;
            }

            public void setAa(String aa) {
                this.aa = aa;
            }

            public String getBb() {
                return bb;
            }

            public void setBb(String bb) {
                this.bb = bb;
            }

            public A(String aa, String bb) {
                this.aa = aa;
                this.bb = bb;
            }

            @Override
            public String toString() {
                return "A{" +
                        "aa='" + aa + '\'' +
                        ", bb='" + bb + '\'' +
                        '}';
            }
        }
        A a = new A("A", "1");
        A b = new A("B", null);

        BeanUtils.copyProperties(a, b);

        A a1 = new A("A", "1");
        A b1 = new A("B", null);
        BeanUtils.copyProperties(b1, a1);
        System.out.println();
    }

    @Test
    public void testLocalDateTime() throws Exception {

        LocalDateTime startTime = YearMonth.parse("2019-10")
                // 第一天
                .atDay(1).atStartOfDay();
        LocalDateTime endTime = YearMonth.parse("2019-10")
                // 加一个月
                .plusMonths(1)
                // 第一天
                .atDay(1).atStartOfDay()
                // 减一毫秒  （减一秒 ChronoUnit.SECONDS）
                .minus(1, ChronoUnit.MILLIS);


        System.out.println("日初：" + startTime);
        System.out.println("日末：" + endTime);
        System.out.println("日初秒时间戳：" + startTime.toEpochSecond(ZoneOffset.ofHours(8)));
        System.out.println("日末秒时间戳：" + endTime.toEpochSecond(ZoneOffset.ofHours(8)));
        System.out.println("日末毫秒时间戳：" + endTime.toInstant(ZoneOffset.ofHours(8)).toEpochMilli());
    }

    @Test
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
            private Map<String, Object> children;

            {
                children = new HashMap<>();
                Map<String, String> hobby = new HashMap<>();
                hobby.put("name", "pig");
                hobby.put("cost", "100$");
                children.put("firstName", "hello");
                children.put("lastName", "word");
                children.put("hobby", hobby);
            }

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
        ImmutableList<A> items = ImmutableList.of(new A("aaaa111", "bbbbb11"));

        ExcelWriter.Column complexColumn = new ExcelWriter.Column();
        complexColumn.setTitle("父单元格");
        complexColumn.setKey("children");
        complexColumn.setChildColumns(Lists.newArrayList(
                new ExcelWriter.Column().setTitle("firstName").setKey("firstName"),
                new ExcelWriter.Column().setTitle("lastName").setKey("lastName"),
                new ExcelWriter.Column().setTitle("hobby").setKey("hobby")
                        .setChildColumns(Lists.newArrayList(
                            new ExcelWriter.Column().setTitle("name").setKey("name"),
                            new ExcelWriter.Column().setTitle("cost").setKey("cost")
                        )
                )
        ));

        ExcelWriter excelWriter = new ExcelWriter()
                .setData(items)
                .setTitle("wes1")
                .addColumn("AA", "aa", ExcelWriter.ColumnType.STRING)
                .addColumn("BB", "aa", ExcelWriter.ColumnType.STRING)
                .addColumn(complexColumn)
                .save();

        excelWriter.createSheet()
                .setData(items)
                .setTitle("wes2")
                .addColumn("AA", "aa", ExcelWriter.ColumnType.STRING)
                .addColumn("BB", "bb", ExcelWriter.ColumnType.STRING);
//                .save();

        excelWriter.writeToFile("d:/test.xlsx");
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
