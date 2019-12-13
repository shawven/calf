package com.starter.demo.support.util.excel;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.NotOfficeXmlFileException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * @author Shoven
 * @date 2019-08-12
 */
public class ExcelReader {
    /**
     * 工作薄
     */
    private Workbook workbook;

    /**
     * 工作表
     */
    private Sheet sheet;

    /**
     * 数据流
     */
    private Stream<Row> stream;

    /**
     * @param name 文件名称
     * @throws IOException
     */
    public ExcelReader(String name) throws IOException {
        this(name, 0);
    }

    /**
     * @param inputStream 文件输入流
     * @throws IOException
     */
    public ExcelReader(InputStream inputStream) throws IOException {
        this(inputStream, 0);
    }

    /**
     * @param name 文件名称
     * @param sheetIndex 工作表索引
     * @throws IOException
     */
    public ExcelReader(String name, int sheetIndex) throws IOException {
        this.workbook = getWorkbook(new File(name));
        switchSheetAt(sheetIndex);
        this.stream = createStream();
    }

    /**
     * @param inputStream 文件输入流
     * @param sheetIndex 工作表索引
     * @throws IOException
     */
    public ExcelReader(InputStream inputStream, int sheetIndex) throws IOException {
        this.workbook = getWorkbook(inputStream);
        switchSheetAt(sheetIndex);
        this.stream = createStream();
    }


    /**
     * @return Stream
     */
    public Stream<Row> stream() {
        return stream;
    }

    /**
     * 读取数据，可定位异常位置
     * 从起始索引到终止索引
     *
     * @param consumer 消费函数
     * @return
     */
    public void read(Consumer<Row> consumer) {
        stream.forEach(row -> {
            try {
                consumer.accept(row);
            } catch (Exception e) {
                if (row != null) {
                    CellAddress address = row.getCurrentAddress();
                    throw new RuntimeException(String.format("第 %s 行 %s 列 %s 单元格：%s",address.getRow() + 1,
                            address.getColumn() + 1, address.toString(), e.getMessage()));
                }
                throw e;
            }
        });
    }

    /**
     * 从多少行开始, n的取值范围[-rowSize，rowSize]
     * n > 0 （n = 1: 起始索引是0）
     * n < 0 （n = -1: 起始索引是rowSize - 1）
     * @param n 行数
     * @return
     */
    public ExcelReader start(int n) {
        if (n != 0) {
            int rowSize = sheet.getLastRowNum();
            int skip = n > 0
                    // 限制起始索引最大不超过rowSize
                    ? Math.min(n, rowSize)
                    // 限制起始索引最小为0
                    : Math.max(0, rowSize + n);
            stream = stream.skip(skip);
        }

        return this;
    }

    /**
     * 从开始位置往后多少行结束，n的取值范围[-rowSize，rowSize]
     * n > 0 （n = 2: 终点索引位置是1）
     * n < 0 （n = -2: 终点索引是 rowSize - 2）
     * start(-3).length(-1):  从倒数第三行至倒数第一行
     *
     * @param n 行数
     * @return
     */
    public ExcelReader length(int n) {
        if (n != 0) {
            int rowSize = sheet.getLastRowNum();
            // 获取终止索引位置
            // 终止位置大于等于0 向后截取, 终止位置小于0 向前截取(加非负数)
            int limit =  n > 0
                    // 限制终止索引最大不超过 rowSize
                    ? Math.min(n, rowSize)
                    // 限制起始索引最小为起始索引
                    : Math.max(0, rowSize + n);
            stream = stream.limit(limit);
        }
        return this;
    }

    public ExcelReader filter(Predicate<Row> lineFilter) {
        stream = stream.filter(lineFilter);
        return this;
    }

    /**
     * 下一个工作表
     *
     * @return
     */
    public ExcelReader nextSheet() {
        switchSheetAt(workbook.getSheetIndex(sheet) + 1);
        return this;
    }

    /**
     * 切换工作表
     *
     * @param sheetIndex
     */
    private void switchSheetAt(int sheetIndex) {
        this.sheet = workbook.getSheetAt(sheetIndex);
    }

    /**
     * 读取Excel测试，兼容 Excel 2003/2007/2010
     *
     * @return
     */
    private Stream<Row> createStream() {
        int rowNum = sheet.getLastRowNum();
        List<Row> rows = new ArrayList<>(rowNum);
        for (int i = 0; i <= rowNum; i++) {
            org.apache.poi.ss.usermodel.Row row = sheet.getRow(i);
            int cellSize = row.getLastCellNum();

            Map<CellAddress, String> line = new LinkedHashMap<>(cellSize);
            for (int j = 0; j < cellSize; j++) {
                Cell cell = row.getCell(j);
                line.put(getAddress(cell, i, j), getValue(cell));
            }
            rows.add(new Row(i, line));
        }
        return rows.stream();
    }

    /**
     * 获取单元格地址
     *
     * @param cell
     * @param i
     * @param j
     * @return
     */
    private CellAddress getAddress(Cell cell, int i, int j) {
        return cell == null ? new CellAddress(i, j) : cell.getAddress();
    }

    /**
     * 获取单元格的值
     *
     * @param cell
     * @return
     */
    private String getValue(Cell cell) {
        if (cell == null) {
            return "";
        }
        Object value = "";
        switch (cell.getCellType()) {
            case NUMERIC:
                value = cell.getNumericCellValue();
                break;
            case STRING:
                value = cell.getStringCellValue();
                break;
            case BOOLEAN:
                value = cell.getBooleanCellValue();
                break;
            case ERROR:
                value = cell.getErrorCellValue();
                break;
            case BLANK:
            default:
        }
        return String.valueOf(value);
    }


    /**
     * 获取工作薄
     *
     * @param file
     * @return
     * @throws IOException
     */
    private Workbook getWorkbook(File file) throws IOException {
        String xls = "xls";
        String xlsx = "xlsx";
        if (!file.exists()) {
            throw new FileNotFoundException(String.format("文件[%s]不存在", file.getAbsolutePath()));
        }
        String fileName = file.getName();
        if (!fileName.endsWith(xls) && !fileName.endsWith(xlsx)) {
            throw new NotOfficeXmlFileException(
                    String.format("文件[%s]不是%s或%s文件", fileName, xls, xlsx));
        }

        return getWorkbook(new FileInputStream(file));
    }


    /**
     * 获取工作薄
     *
     * @param inputStream
     * @return
     * @throws IOException
     */
    private Workbook getWorkbook(InputStream inputStream) throws IOException {
        // 采用inputStream时不知道excel格式，逐个尝试
        // 但直接使用inputStream 创建workbook会污染inputStream导致不能后续尝试，所以每次尝试构造一个新的inputStream
        byte[] bytes = getBytes(inputStream);
        Workbook wb;
        try {
            // Excel 2007
            wb = new XSSFWorkbook(new ByteArrayInputStream(bytes));
        } catch (Exception e) {
            // Excel 2003
            wb = new HSSFWorkbook(new ByteArrayInputStream(bytes));
        }
        return wb;
    }

    private byte[] getBytes(InputStream is) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        for(int n; -1 != (n = is.read(buffer));) {
            out.write(buffer, 0, n);
        }
        return out.toByteArray();
    }

    /**
     * excel行
     */
    public class Row {

        /**
         * 单元格列名地址索引（第几个如第B列或第2列）
         */
        private Map<String, CellAddress> indexes;

        /**
         * 单元格数据映射（地址对应数据）
         */
        private Map<CellAddress, ?> row;

        private int rowIndex;

        /**
         * 当前地址
         */
        private CellAddress currentAddress;

        private Row(int rowIndex, Map<CellAddress, ?> row) {
            this.rowIndex = rowIndex;
            this.row = row;
            this.indexes = new HashMap<>();
            for (CellAddress address : row.keySet()) {
                indexes.put(address.getColumn() + "", address);
                indexes.put(CellReference.convertNumToColString(address.getColumn()) + "", address);
            }
        }

        /**
         * 获取第几列, 如2列：get("B") 或者第2列 get("2")
         *
         * @param key
         * @return
         */
        public String get(String key) {
            if (key == null) {
                return null;
            }
            this.currentAddress = indexes.get(key);
            Object value = row.get(currentAddress);
            return value == null ? null : value.toString().trim();
        }

        public int getInt(String key) {
            String value = get(key);
            return Double.valueOf(value).intValue();
        }

        public long getLong(String key) {
            String value = get(key);
            return Double.valueOf(value).longValue();
        }

        public double getDouble(String key) {
            String value = get(key);
            return Double.parseDouble(value);
        }

        public int getRowIndex() {
            return rowIndex;
        }

        public CellAddress getCurrentAddress() {
            return currentAddress;
        }

        @Override
        public boolean equals(Object another) {
            if (this == another)  {
                return true;
            }
            if (another == null || getClass() != another.getClass()) {
                return false;
            }
            Row anotherRow = (Row) another;
            return Objects.equals(this.row.values().toString(),
                    anotherRow.row.values().toString());
        }

        @Override
        public int hashCode() {
            return Objects.hash(row.values().toString());
        }

        @Override
        public String toString() {
            return "Line{" +
                    "row=" + row.values() +
                    '}';
        }
    }
}
