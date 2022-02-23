package com.github.shawven.calf.util;

import com.github.shawven.calf.util.excel.ExcelReader;
import com.github.shawven.calf.util.excel.ExcelWriter;
import com.google.common.collect.*;
import com.nlf.calendar.Lunar;
import com.nlf.calendar.Solar;
import com.nlf.calendar.util.HolidayUtil;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xssf.usermodel.*;
import org.dom4j.Document;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.BeanUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkNotNull;

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

    static Semaphore lock = new Semaphore(-1, true);


    static class task  {

        int a = 1;
        int b = 2;
        int c = 3;
        int d = 4;

        private  void write() {
            a = 11;
            b = 22;
            c = 33;
            synchronized (this) {
                d = 44;
            }
        }

        private void read() {
            int dd = 0;
            synchronized (this) {
                dd = d;
            }
            int aa = a;
            int bb = b;
            int cc = c;
            if (dd == 44 ) {
                if (aa != 11 || bb !=22 || cc != 33) {
                    throw new RuntimeException( " " + aa + " " + bb + " " + cc + " " + dd);
                }

            }

        }
    };


    @Test
    public void testSync() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(6);

        for (int i = 0; i < 10000000; i++) {
            task task = new task();
            executor.execute(() -> {
                task.write();
            });
            executor.execute(() -> {
                task.read();
            });
        }

        executor.shutdown();
        while (!executor.isTerminated()) {
            TimeUnit.SECONDS.sleep(1);
        }
        System.out.println(executor.isTerminated());
    }

    @Test
    public void testBitmap() {
        Integer i = null;

        try {
            i.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            checkNotNull(i).toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Test
    public void testString() {
        String s = new String("code");
        changeString(s);
        Assert.assertEquals("code", s);
    }

    public void changeString(String s) {
        s = "123";
    }

    @Test
    public void test() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(3);
        for (int i = 0; i < 9; i++) {
            executor.submit(() -> {
                try {
                    lock.acquire();
                    try {
                        System.out.println(Thread.currentThread().getName() + " acquire");
                        TimeUnit.SECONDS.sleep(2);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    System.out.println(Thread.currentThread().getName() + " release");
                    lock.release();
                }
            });
        }
        executor.shutdown();
        while (!executor.isTerminated());
    }


    @Test
    public void testCopyProperties() {
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
    public  void testXml() throws Exception {
        SAXReader reader = new SAXReader();
        Document document = reader.read(new File("E:/转换工具/树立水.xml"));

        FileWriter fileWriter = new FileWriter("E:/转换工具/树立水_美化.xml");
        OutputFormat format = OutputFormat.createPrettyPrint();
        format.setTrimText(false);
        XMLWriter writer = new XMLWriter(fileWriter, format);
        writer.write(document);
        writer.close();
    }


    @Test
    public void testRangeMap() {
        RangeMap<Integer, String> rangeMap = TreeRangeMap.create();
        rangeMap.put(Range.greaterThan(1), "1");
        rangeMap.put(Range.greaterThan(2), "2");
        rangeMap.put(Range.greaterThan(3), "3");
        rangeMap.put(Range.greaterThan(4), "4");
        rangeMap.put(Range.greaterThan(5), "5");

        rangeMap.get(3);
    }

    @Test
    public void testNodeTree2() {
        class Menu implements NodeTree.Node<Menu> {

            @Override
            public List<Menu> getChildren() {
                return null;
            }

            @Override
            public void setChildren(List<Menu> children) {

            }
        }

    }
    @Test
    public void testNodeTree() {
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

            @Override
            public String toString() {
                return "Menu{" +
                        "name='" + name + '\'' +
                        ", id='" + id + '\'' +
                        '}';
            }
        }


        List<Menu> menus = Lists.newArrayList(
                new Menu("B", "2"),
                new Menu("BB", "22"),
                new Menu("A", "1"),
                new Menu("AA", "11"),
                new Menu("AAA", "111"),
                new Menu("AAB", "112"),
                new Menu("AB", "12"),
                new Menu("ABA", "121"),
                new Menu("ABB", "122")

        );

        List<Menu> tree = NodeTree.<Menu, Menu>from(menus)
                .rootFilter(menu -> menu.id.length() == 1)
                .childFilter((parent, child) -> child.name.startsWith(parent.name)
                        && child.name.length() - 1 == parent.name.length()
                )
                .build();

        System.out.println(tree);
        System.out.println(NodeTree.traceNode(tree, menu -> menu.id.equals("12")));
        System.out.println(NodeTree.traceNode(tree, menu -> menu.id.equals("112")));
        System.out.println(NodeTree.traceNode(tree, menu -> menu.id.equals("122")));
    }

    @Test
    public void testExcelReader() throws Exception {
        ExcelReader excelReader = new ExcelReader("C:\\Users\\XW\\Documents\\WeChat Files\\vivid44165\\FileStorage\\File\\2020-07\\中国银行HISXLS-20200401-20200430-08605054(1).xls");

        excelReader.firstSheet().read(dataRow -> {
            System.out.println(dataRow);
        });

        excelReader.allSheetRead(sheetName -> {
            if (sheetName.endsWith("多账户查询业务处理结果")) {
                return dataRow -> {
                    System.out.println(dataRow);
                };
            } else if (sheetName.endsWith("多账")) {
                return dataRow -> {
                    System.out.println(dataRow);
                };
            }
            return null;
        });
    }

    @Test
    public void testExcelWriter() throws Exception {
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
        ImmutableList<A> items = ImmutableList.of(
                new A("aaaa11", "bbbbb11"),
                new A("aaaa22", "bbbbb22"),
                new A("aaaa33", "bbbbb33")
        );

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
                .setTitle("wes111")
                .addColumn("AA", "aa", ExcelWriter.ColumnType.STRING)
                .addColumn("BB", "aa", ExcelWriter.ColumnType.STRING)
                .addColumn(complexColumn)
                .save();

        excelWriter.createSheet()
                .setData(items)
                .setTitle("wes22")
                .addColumn("AA", "aa", ExcelWriter.ColumnType.STRING)
                .addColumn("BB", "bb", ExcelWriter.ColumnType.STRING)
                .save();

        excelWriter.writeToFile("d:/test.xlsx");
    }

    @Test
    public void testLockedExcelWriter() throws IOException {
        XSSFWorkbook wb = new XSSFWorkbook();
        XSSFSheet sheet = wb.createSheet("sheet名称");

        XSSFCellStyle lockstyle = wb.createCellStyle();
        lockstyle.setLocked(true);//设置锁定

        lockstyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        byte[] rgb = {(byte) 240, (byte) 240, (byte) 240};
        lockstyle.setFillForegroundColor(new XSSFColor(rgb, new DefaultIndexedColorMap()));

        lockstyle.setBorderTop(BorderStyle.THIN);
        lockstyle.setBorderLeft(BorderStyle.THIN);
        lockstyle.setBorderRight(BorderStyle.THIN);
        lockstyle.setBorderBottom(BorderStyle.THIN);
        lockstyle.setBottomBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        lockstyle.setLeftBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        lockstyle.setRightBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        lockstyle.setTopBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());

        XSSFCellStyle unlockStyle = wb.createCellStyle();
        unlockStyle.setLocked(false);//设置未锁定


        for (int i = 0; i < 10; i++) {
            XSSFRow row = sheet.createRow(i);
            for (int j = 0; j < 10; j++) {
                XSSFCell cell = row.createCell(j);
                cell.setCellStyle(unlockStyle);//默认是锁定状态；将所有单元格设置为：未锁定；然后再对需要上锁的单元格单独锁定
                if (j == 1) {//这里可以根据需要进行判断;我这就将第2列上锁了
                    cell.setCellStyle(lockstyle);//将需要上锁的单元格进行锁定
                    cell.setCellValue("上锁了");
                } else {
                    cell.setCellValue("没上锁了");
                }
            }
        }
        //sheet添加保护，这个一定要否则光锁定还是可以编辑的
        sheet.protectSheet("123456");
        FileOutputStream os = new FileOutputStream("D:\\" + System.currentTimeMillis() + ".xlsx");
        wb.write(os);
        os.close();
    }




    public static class ChinaDate extends Lunar {

        Map<String, String> sFtv = new HashMap<String, String>();
        Map<String, String> lFtv = new HashMap<String, String>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMdd");
        {
            // 国历节日
            sFtv.put("0101", "元旦");
            sFtv.put("0501", "劳动节");
            sFtv.put("1001", "国庆节");

            //农历节日
            lFtv.put("0101", "春节");
            lFtv.put("0505", "端午");
            lFtv.put("0815", "中秋");
        }

        public ChinaDate() {
        }

        public ChinaDate(int lunarYear, int lunarMonth, int lunarDay) {
            super(lunarYear, lunarMonth, lunarDay);
        }

        public ChinaDate(int lunarYear, int lunarMonth, int lunarDay, int hour, int minute, int second) {
            super(lunarYear, lunarMonth, lunarDay, hour, minute, second);
        }

        public ChinaDate(Date date) {
            super(date);
        }

        public String getHoliday() {
            String val = lFtv.get(formatter.format(MonthDay.of(getMonth(), getDay())));
            if (val != null) {
                return val;
            }
            if (Math.abs(getMonth()) == 12 && getDay() >= 29 && getYear() != next(1).getYear()) {
                return "除夕";
            }

            Solar solar = getSolar();
            return sFtv.get(formatter.format(MonthDay.of(solar.getMonth(), solar.getDay())));
        }

    }
    @Test
    public void testLunar() {

        //今天
//        Lunar date = new Lunar();

        //指定阴历的某一天

        ChinaDate date = new ChinaDate(Times.toDate(LocalDate.of(2022,2,19)));
        System.out.println(date);
        System.out.println(date.getJieQi());

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


}
