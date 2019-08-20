package com.test.support.util.excel;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.NotOfficeXmlFileException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;

/**
 * @author Shoven
 * @date 2019-08-12
 */
public class ExcelReader {

    private static final String EXCEL_XLS = "xls";
    private static final String EXCEL_XLSX = "xlsx";

    /**
     * 起始位置
     */
    private int startIndex;

    /**
     * 终止位置 (负数为倒数第几个)
     */
    private int endIndex;

    /**
     * 工作薄
     */
    private Workbook workbook;

    /**
     * 工作表
     */
    private Sheet sheet;

    /**
     * @param name 文件名称
     * @throws IOException
     */
    public ExcelReader(String name) throws IOException {
        this(name, 0);
    }

    /**
     * @param name 文件名称
     * @param sheetIndex 打开的工资表
     * @throws IOException
     */
    public ExcelReader(String name, int sheetIndex) throws IOException {
        this.workbook = getWorkbook(new File(name));
        switchSheetAt(sheetIndex);
    }

    public int read(Consumer<Line<String>> consumer) {
        return read(0, endIndex, consumer);
    }

    /**
     * 左闭右开区间[startIndex, endIndex)
     *
     * @param startIndex 开始行索引
     * @param endIndex   结束行索引
     * @param consumer   行数据消费者函数
     * @return
     */
    public int read(int startIndex, int endIndex, Consumer<Line<String>> consumer) {
        if (startIndex < 0) {
            throw new IllegalArgumentException(Integer.toString(startIndex));
        } else {
            this.startIndex = getStartIndex(startIndex);
        }
        // 这是索引位置不是长度
        // [0, 5) 长度是5 最后索引位置是4
        this.endIndex = getEndIndex(endIndex);

        List<Map<CellAddress, String>> rows = readExcel();

        Line<String> line = null;
        try {
            for (Map<CellAddress, String> row : rows) {
                line = new Line<>(row);
                consumer.accept(line);
            }
        } catch (Exception e) {
            if (line != null) {
                CellAddress address = line.getCurrentAddress();
                throw new RuntimeException(String.format("第 %s 行 %s 列 %s 单元格：%s",address.getRow() + 1,
                        address.getColumn() + 1, address.toString(), e.getMessage()));
            }
            throw e;
        }

        return rows.size();
    }

    /**
     * 跳过多少行
     *
     * @param n 行数
     * @return
     */
    public ExcelReader skip(int n) {
        if (n < 0) {
            throw new IllegalArgumentException(Integer.toString(startIndex));
        } else {
            this.startIndex = getStartIndex(startIndex + n);
        }
        return this;
    }

    /**
     * 限制多少行，实际最后的一行索引位置是startIndex + n - 1
     *
     * @param n 行数
     * @return
     */
    public ExcelReader limit(int n) {
        if (n != Integer.MAX_VALUE) {
            this.startIndex = getEndIndex(startIndex + n);
        }
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
        this.startIndex = 0;
        this.endIndex = Integer.MAX_VALUE;
        this.sheet = workbook.getSheetAt(sheetIndex);
    }

    /**
     * 读取Excel测试，兼容 Excel 2003/2007/2010
     *
     * @return
     */
    public List<Map<CellAddress, String>> readExcel() {
        List<Map<CellAddress, String>> list = new ArrayList<>();
        for (int i = startIndex; i < endIndex; i++) {
            Row row = sheet.getRow(i);
            int cellSize = row.getLastCellNum();

            Map<CellAddress, String> line = new LinkedHashMap<>(cellSize);
            for (int j = 0; j < cellSize; j++) {
                Cell cell = row.getCell(j);
                line.put(getAddress(cell, i, j), getValue(cell));
            }
            list.add(line);
        }

        return list;
    }

    /**
     * 获取开始索引位置
     *
     * @param startIndex
     * @return
     */
    private int getStartIndex(int startIndex) {
        int rowSize = sheet.getLastRowNum();
        return Math.min(startIndex, rowSize);
    }

    /**
     * 获取终止索引位置
     * 终止位置大于等于0 向后截取, 终止位置小于0 向前截取(加非负数)
     *
     * @param endIndex
     * @return
     */
    private int getEndIndex(int endIndex) {
        int rowSize = sheet.getLastRowNum();
        return endIndex > 0 ? Math.min(endIndex, rowSize) : rowSize + endIndex;
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
        if (!file.exists()) {
            throw new FileNotFoundException(String.format("文件[%s]不存在", file.getAbsolutePath()));
        }
        String fileName = file.getName();
        if (!fileName.endsWith(EXCEL_XLS) && !fileName.endsWith(EXCEL_XLSX)) {
            throw new NotOfficeXmlFileException(
                    String.format("文件[%s]不是%s或%s文件", fileName, EXCEL_XLS, EXCEL_XLSX));
        }

        FileInputStream inputStream = new FileInputStream(file);
        Workbook wb;
        // Excel 2003
        if (file.getName().endsWith(EXCEL_XLS)) {
            wb = new HSSFWorkbook(inputStream);
            // Excel 2007
        } else {
            wb = new XSSFWorkbook(inputStream);
        }
        return wb;
    }

    /**
     * excel行
     * @param <T>
     */
    public class Line<T> {

        /**
         * 单元格列名地址索引（第几个如第B列或第2列）
         */
        private Map<String, CellAddress> indexes;

        /**
         * 单元格数据映射（地址对应数据）
         */
        private Map<CellAddress, T> row;

        /**
         * 当前地址
         */
        private CellAddress currentAddress;

        private Line(Map<CellAddress, T> row) {
            this.row = row;
            this.indexes = new HashMap<>();
            for (CellAddress address : row.keySet()) {
                indexes.put(address.getColumn() + "", address);
                indexes.put(CellReference.convertNumToColString(address.getColumn()) + "", address);
            }
        }

        /**
         * 获取第几列
         * 获取第B列：get("B") 或者第2列 get("2")
         *
         * @param index
         * @return
         */
        public T get(String index) {
            if (index == null) {
                return null;
            }
            this.currentAddress = indexes.get(index);
            return row.get(currentAddress);
        }

        private CellAddress getCurrentAddress() {
            return currentAddress;
        }

        @Override
        public String toString() {
            return "Line{" +
                    "row=" + row.values() +
                    '}';
        }
    }
}
