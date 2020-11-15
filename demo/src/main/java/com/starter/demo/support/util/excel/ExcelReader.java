package com.starter.demo.support.util.excel;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.NotOfficeXmlFileException;
import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.emptyMap;

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
    private Stream<DataRow> stream;

    /**
     * @param name 文件名称
     * @throws IOException IO异常
     */
    public ExcelReader(String name) throws IOException {
        this(name, 0);
    }

    /**
     * @param inputStream 文件输入流
     * @throws IOException IO异常
     */
    public ExcelReader(InputStream inputStream) throws IOException {
        this(inputStream, 0);
    }

    /**
     * @param name 文件名称
     * @param sheetIndex 工作表索引
     * @throws IOException IO异常
     */
    public ExcelReader(String name, int sheetIndex) throws IOException {
        this.workbook = getWorkbook(new File(name));
    }

    /**
     * @param inputStream 文件输入流
     * @param sheetIndex 工作表索引
     * @throws IOException IO异常
     */
    public ExcelReader(InputStream inputStream, int sheetIndex) throws IOException {
        this.workbook = getWorkbook(inputStream);
    }

    /**
     * 读取数据，可定位异常位置
     * 从起始索引到终止索引
     *
     * @param consumer 消费函数
     */
    public void read(Consumer<DataRow> consumer) {
        ensureSelectSheet();
        stream.forEach(wrapConsumer(consumer));
    }

    public Stream<DataRow> stream() {
        return stream;
    }

    public void allSheetRead(Function<String, Consumer<DataRow>> function) {
        int numberOfSheets = workbook.getNumberOfSheets();
        for (int i = 0; i < numberOfSheets; i++) {
            Sheet sheet = workbook.getSheetAt(i);
            createStream(sheet).forEach(dataRow -> {
                Consumer<DataRow> consumer = function.apply(sheet.getSheetName());
                if (consumer != null) {
                    wrapConsumer(consumer).accept(dataRow);
                }
            });
        }
    }

    private Consumer<DataRow> wrapConsumer(Consumer<DataRow> consumer) {
        return dataRow -> {
            try {
                consumer.accept(dataRow);
            } catch (Exception e) {
                if (dataRow != null) {
                    CellAddress address = dataRow.getAddress();
                    throw new RuntimeException(String.format("第 %s 行 %s 列 %s 单元格：%s",address.getRow() + 1,
                            address.getColumn() + 1, address.toString(), e.getMessage()));
                }
                throw e;
            }
        };
    }

    public Map<String, Stream<DataRow>> allSheetStream() {
        int numberOfSheets = workbook.getNumberOfSheets();
        Map<String, Stream<DataRow>> map = new HashMap<>();
        for (int i = 0; i < numberOfSheets; i++) {
            Sheet sheet = workbook.getSheetAt(i);
            Stream<DataRow> stream = createStream(sheet);
            map.put(sheet.getSheetName(), stream);
        }
        return map;
    }

    public ExcelReader firstSheet() {
        sheet = workbook.getSheetAt(0);
        stream = createStream(sheet);
        return this;
    }

    public ExcelReader endSheet() {
        sheet = workbook.getSheetAt((workbook.getNumberOfSheets() - 1));
        stream = createStream(sheet);
        return this;
    }

    /**
     * 切换工作表
     *
     * @param index 工作表序号
     */
    public ExcelReader sheetAt(int index) {
        sheet = workbook.getSheetAt(index);
        stream = createStream(sheet);
        return this;
    }

    /**
     * 获取数据行
     *
     * @param i 行号
     * @return
     */
    public DataRow getDataRow(int i) {
        ensureSelectSheet();
        int rowNum = sheet.getLastRowNum();
        if (i < 0 || i >= rowNum) {
            throw new IndexOutOfBoundsException("数据行号有误");
        }
        return getDataRow(sheet, i);
    }

    /**
     * 确保选择sheet
     */
    private void ensureSelectSheet() {
        if (stream == null) {
            throw new RuntimeException("未选择Sheet");
        }
    }

    /**
     * 读取Excel测试，兼容 Excel 2003/2007/2010
     *
     * @return Stream
     */
    private Stream<DataRow> createStream(Sheet sheet) {
        int rowNum = sheet.getLastRowNum();
        List<DataRow> dataRows = new ArrayList<>(rowNum);
        for (int i = 0; i <= rowNum; i++) {
            dataRows.add(getDataRow(sheet, i));
        }
        return dataRows.stream();
    }

    /**
     * 获取数据行
     *
     * @param sheet 工作表
     * @param i 行号
     * @return DataRow
     */
    private DataRow getDataRow(Sheet sheet, int i) {
        Row row = sheet.getRow(i);
        Map<CellAddress, String> line;
        int cellSize;
        if (row == null || (cellSize = Math.max(row.getLastCellNum(), 0)) == 0) {
            line = emptyMap();
        } else {
            line = new LinkedHashMap<>(cellSize);
            for (int j = 0; j < cellSize; j++) {
                Cell cell = row.getCell(j);
                line.put(getAddress(cell, i, j), getValue(cell));
            }
        }
        return new DataRow(i, line);
    }

    /**
     * 获取单元格地址
     *
     * @param cell Cell
     * @param row 行
     * @param col 列
     * @return 单元格地址
     */
    private CellAddress getAddress(Cell cell, int row, int col) {
        return cell == null ? new CellAddress(row, col) : cell.getAddress();
    }

    /**
     * 获取单元格的值
     *
     * @param cell Cell
     * @return String
     */
    private String getValue(Cell cell) {
        if (cell == null) {
            return "";
        }
        Object value ;
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
                value = "";
        }
        return String.valueOf(value);
    }


    /**
     * 获取工作薄
     *
     * @param file 文件
     * @return Workbook
     * @throws IOException IO异常
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
     * @param inputStream 输入流
     * @return Workbook
     * @throws IOException IO异常
     */
    private Workbook getWorkbook(InputStream inputStream) throws IOException {
        // 采用inputStream时不知道excel格式，逐个尝试
        // 但直接使用inputStream 创建workbook会污染inputStream导致不能后续尝试，所以尝试构造一个新的inputStream
        InputStream is = newByteArrayInputStream(inputStream);
        // 延迟解析比率
        ZipSecureFile.setMinInflateRatio(-1.0d);
        Workbook wb;
        try {
            // Excel 2007
            wb = new XSSFWorkbook(is);
        } catch (Exception e) {
            // Excel 2003
            wb = new HSSFWorkbook(is);
        }
        return wb;
    }

    private InputStream newByteArrayInputStream(InputStream is) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        for(int n; -1 != (n = is.read(buffer));) {
            out.write(buffer, 0, n);
        }
        return new ByteArrayInputStream(out.toByteArray());
    }

    /**
     * excel行
     */
    public static class DataRow {

        /**
         * 单元格列名地址索引（第几个如第B列或第2列）
         */
        private Map<String, CellAddress> indexes;

        /**
         * 单元格数据映射（地址对应数据）
         */
        private Map<CellAddress, String> row;

        private int rowIndex;

        /**
         * 当前地址
         */
        private CellAddress currentAddress;

        private DataRow(int rowIndex, Map<CellAddress, String> row) {
            this.rowIndex = rowIndex;
            this.row = row;
            this.indexes = new HashMap<>();
            for (CellAddress address : row.keySet()) {
                indexes.put(address.getColumn() + "", address);
                indexes.put(CellReference.convertNumToColString(address.getColumn()) + "", address);
            }
        }

        /**
         * 获取第几列, 如第4列：get("D") 或者get("3")
         *
         * @param key 列名或第几列
         * @return String
         */
        public String get(String key) {
            if (key == null || key.isEmpty()) {
                throw new IllegalArgumentException("key不能为空");
            }
            this.currentAddress = indexes.get(key);
            return row.get(currentAddress);
        }

        public int getInt(String key) {
            String value = get(key);
            if (value == null || value.isEmpty()) {
                return 0;
            }
            return Double.valueOf(value).intValue();
        }

        public long getLong(String key) {
            String value = get(key);
            if (value == null || value.isEmpty()) {
                return 0L;
            }
            return Double.valueOf(value).longValue();
        }

        public double getDouble(String key) {
            String value = get(key);
            if (value == null || value.isEmpty()) {
                return 0D;
            }
            return Double.parseDouble(value);
        }

        public int getRowIndex() {
            return rowIndex;
        }

        public CellAddress getAddress() {
            return currentAddress;
        }

        public Collection<String> getValues() {
            return row.values();
        }

        @Override
        public boolean equals(Object another) {
            if (this == another)  {
                return true;
            }
            if (another == null || getClass() != another.getClass()) {
                return false;
            }
            DataRow anotherRow = (DataRow) another;
            return Objects.equals(this.row.values().toString(),
                    anotherRow.row.values().toString());
        }

        @Override
        public int hashCode() {
            return Objects.hash(row.values().toString());
        }

        @Override
        public String toString() {
            return String.valueOf(row.values());
        }
    }
}
