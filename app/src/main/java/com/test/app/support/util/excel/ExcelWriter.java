package com.test.app.support.util.excel;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import static java.nio.charset.StandardCharsets.ISO_8859_1;

/**
 * Excel工具 "xlsx"类型
 *
 * @author Shoven
 * @date 2018-12-27
 */
public class ExcelWriter<T> {

    /**
     * 字体名称
     */
    private static final String DEFAULT_FONT_NAME = "宋体";
    /**
     * 表头高度
     */
    private static final int DEFAULT_HEADER_HEIGHT = 20;
    /**
     * 列名高度
     */
    private static final int DEFAULT_COLUMN_HEIGHT = 20;
    /**
     * 表头字体大小
     */
    private static final short DEFAULT_HEADER_FONT_SIZE = 12;
    /**
     * 行字体大小
     */
    private static final short DEFAULT_ROW_FONT_SIZE = 11;

    /**
     * 数据行位置
     */
    private int rowIndex;

    /**
     * 表格头部索引
     */
    private int headerIndex;

    /**
     * 列名
     */
    private List<Column> columns;

    /**
     * 表名
     */
    private String headerName;

    /**
     * 数据
     */
    private List<T> data;

    /**
     * 工作薄
     */
    private Workbook workbook;

    /**
     * 工作表
     */
    private Sheet sheet;

    /**
     * 后置任务
     */
    private List<Runnable> postTask;

    public ExcelWriter() {
        columns = new ArrayList<>();
    }


    public ExcelWriter<T> setColumn(String title, String key) {
        return setColumn(title, key, ColumnType.STRING);
    }

    public ExcelWriter<T> setColumn(String title, String key, ColumnType columnType) {
        return setColumn(title, key, 10, columnType, null);
    }

    public ExcelWriter<T> setColumn(String title, String key, ColumnAlign columnAlign) {
        return setColumn(title, key, 10, ColumnType.STRING, columnAlign);
    }

    public ExcelWriter<T> setColumn(String title, String key, int width) {
        return setColumn(title, key, width, ColumnType.STRING, null);
    }

    public ExcelWriter<T> setColumn(String title, String key, int width, ColumnType columnType, ColumnAlign columnAlign) {
        Column column = new Column();
        column.setTitle(title);
        column.setKey(key);
        column.setWidth(width);
        column.setColumnType(columnType);
        column.setColumnAlign(columnAlign);
        this.columns.add(column);
        return this;
    }

    public ExcelWriter<T> setColumn(String title, Function<T, Object> keyFunc) {
        return setColumn(title, keyFunc, ColumnType.STRING);
    }

    public ExcelWriter<T> setColumn(String title, Function<T, Object> keyFunc, ColumnType columnType) {
        return setColumn(title, keyFunc, 10, columnType, null);
    }

    public ExcelWriter<T> setColumn(String title, Function<T, Object> keyFunc, ColumnAlign columnAlign) {
        return setColumn(title, keyFunc, 10, ColumnType.STRING, columnAlign);
    }

    public ExcelWriter<T> setColumn(String title, Function<T, Object> keyFunc, int width) {
        return setColumn(title, keyFunc, width, ColumnType.STRING, null);
    }

    public ExcelWriter<T> setColumn(String title, Function<T, Object> keyFunc, int width, ColumnType columnType,
                                    ColumnAlign columnAlign) {
        Column column = new Column();
        column.setTitle(title);
        column.setKeyFunc(keyFunc);
        column.setWidth(width);
        column.setColumnType(columnType);
        column.setColumnAlign(columnAlign);
        this.columns.add(column);
        return this;
    }

    public ExcelWriter<T> setColumn(Column column) {
        this.columns.add(column);
        return this;
    }

    /**
     * 自定义添加行
     *
     * @param rowIndex 指定行号
     * @param consumer 具体逻辑
     * @return ExcelWriter
     */
    public ExcelWriter<T> addLine(int rowIndex, Consumer<Row> consumer) {
        if (postTask == null) {
            postTask = new ArrayList<>();
        }
        postTask.add(() -> {
            Row row = sheet.createRow(rowIndex);
            consumer.accept(row);
        });
        return this;
    }

    /**
     * 末尾添加一行
     *
     * @param consumer 具体逻辑
     * @return ExcelWriter
     */
    public ExcelWriter<T> addEndLine(BiConsumer<Workbook, Row> consumer) {
        if (postTask == null) {
            postTask = new ArrayList<>();
        }
        postTask.add(() -> {
            ++rowIndex;
            Row row = sheet.createRow(rowIndex);
            consumer.accept(workbook, row);
        });
        return this;
    }

    /**
     * 设置表头名
     *
     * @param headerName 名称
     * @return ExcelWriter
     */
    public ExcelWriter<T> setHeaderName(String headerName) {
        this.headerName = headerName;
        return this;
    }

    /**
     * 设置数据
     *
     * @param data 数据集合
     * @return ExcelWriter
     */
    public ExcelWriter<T> setData(List<T> data) {
        this.data = data;
        return this;
    }

    /**
     * 设置表格填充起始行
     *
     * @param rowIndex 行号
     * @return ExcelWriter
     */
    public ExcelWriter<T> setStartRowIndex(int rowIndex) {
        this.headerIndex = this.rowIndex = rowIndex;
        return this;
    }

    /**
     * 写到输出流
     *
     * @param out 输出流
     * @throws IOException Io Exception
     */
    public void write(OutputStream out) throws IOException {
        createSheet();
        workbook.write(out);
        out.flush();
        out.close();
    }

    /**
     * 通过http下载
     *
     * @param response Http响应对象
     * @throws IOException Io Exception
     */
    public void writeToHttpResponse(HttpServletResponse response, String fileName) throws IOException {
        if (fileName == null) {
            fileName = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHiiss"));
        }
        if (!fileName.endsWith(".xlsx")) {
            fileName += ".xlsx";
        }

        createSheet();
        response.reset();
        response.setContentType("application/vnd.ms-struct;charset=utf-8");
        response.setHeader("Content-Disposition", "attachment;filename=" + new String(fileName.getBytes(), ISO_8859_1));

        ServletOutputStream out = response.getOutputStream();
        workbook.write(out);
        out.flush();
        out.close();
    }

    /**
     * 写到文件
     *
     * @param fileName 文件名
     * @throws IOException Io Exception
     */
    public void writeToFile(String fileName) throws IOException {
        if (fileName == null) {
            fileName = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHiiss"));
        }
        if (!fileName.endsWith(".xlsx")) {
            fileName += ".xlsx";
        }

        createSheet();
        FileOutputStream out = new FileOutputStream(fileName);
        workbook.write(out);
        out.flush();
        out.close();
    }

    /**
     * 创建工作表
     */
    private void createSheet() {
        workbook = new XSSFWorkbook();
        // 创建第一个sheet（页）
        this.sheet = workbook.createSheet(headerName == null ? "Sheet1" : headerName);

        // 创建表头
        if (headerName != null) {
            createTableHeader();
        }

        // 创建列标题
        if (columns.isEmpty()) {
            throw new IllegalArgumentException("未设置列标题");
        }
        createColumn();

        // 创建数据单元格
        if (data == null) {
            throw new IllegalArgumentException("未设置单元格数据行");
        }
        createDataArea();

        // 执行后置任务
        if (postTask != null) {
            postTask.forEach(Runnable::run);
        }
    }

    /**
     * 创建表头
     */
    private void createTableHeader() {
        Cell tableHeader = sheet.createRow(headerIndex).createCell(0);
        int size = columns.size();
        if (size > 1) {
            sheet.addMergedRegion(new CellRangeAddress(headerIndex, headerIndex, 0, size - 1));
        }
        tableHeader.setCellValue(headerName);
        setTableHeaderStyle(tableHeader);
    }

    /**
     * 创建单列
     */
    private void createColumn() {
        if (maxColumnRowNum(columns) == 1) {
            rowIndex++;
            Row row = sheet.createRow(rowIndex);
            for (int i = 0, columnsSize = columns.size(); i < columnsSize; i++) {
                Column column = columns.get(i);
                // 创建单列
                createSingleColumn(row, column, i);
            }
        } else {
            // 创建复合列
            createComplexColumns(columns, 0);
        }
    }

    /**
     * 创建复合列
     *
     * @param columns 列集合
     * @param columns 列偏移量 二级列集合从0开始遍历要加上父列列索引为偏移量
     */
    private void createComplexColumns(List<Column> columns, int columnOffset) {
        int startRowIndex = rowIndex + 1;
        int stopRowIndex = rowIndex + maxColumnRowNum(columns);

        while (rowIndex < stopRowIndex) {
            Row row = sheet.createRow(++rowIndex);
            for (int i = 0, columnsSize = columns.size(); i < columnsSize; i++) {
                Column column = columns.get(i);
                int columnIndex = columnOffset + i;
                if (column.isComplex()) {
                    List<Column> childColumns = column.getChildColumns();
                    // 创建父单元格
                    createSingleColumn(row, column, i);
                    // 创建子单元格集合
                    createComplexColumns(childColumns, columnIndex);
                    // 子列集合大于1列，表格会扩充列此时从新合并父单元格和头部
                    if (childColumns.size() > 1) {
                        // 合并父单元格
                        sheet.addMergedRegion(new CellRangeAddress(startRowIndex, startRowIndex,
                                columnIndex, columnOffset + columns.size()));
                        // 合并表格头部
                        if (headerName != null) {
                            sheet.addMergedRegion(new CellRangeAddress(headerIndex, headerIndex,
                                    0, columnOffset + columns.size()));
                        }
                    }
                } else {
                    // 创建列
                    createSingleColumn(row, column, columnIndex);
                    sheet.addMergedRegion(new CellRangeAddress(startRowIndex, stopRowIndex,
                            columnIndex, columnIndex));
                }
            }
        }
    }

    /**
     * 创建单列
     *
     * @param row         行
     * @param column      列
     * @param columnIndex 列下标
     */
    private void createSingleColumn(Row row, Column column, int columnIndex) {
        Cell columnTitle = row.createCell(columnIndex);
        columnTitle.setCellValue(column.getTitle());
        //设置列宽高
        int width = column.getWidth();
        sheet.setColumnWidth(columnIndex, width != 0 ? 256 * width * 2 : 2560 * 2);
        setColumnStyle(columnTitle);
    }

    /**
     * 创建数据域
     */
    private void createDataArea() {
        // 从数据行索引起点开始 遍历数据源数据
        for (Object line : data) {
            Row row = sheet.createRow(++rowIndex);
            for (int i = 0, columnsSize = columns.size(); i < columnsSize; i++) {
                Column column = columns.get(i);
                if (column.isComplex()) {
                    Object childLine = getCellValue(line, column);
                    createDataAreaOfComplexColumn(childLine, row, column.getChildColumns(), i);
                } else {
                    createDataCell(line, row, i, column);
                }
            }
        }
    }

    /**
     * 创建复合单元格的行数据
     *
     * @param line         行数据
     * @param row          Excel行对象
     * @param columns      列集合
     * @param columnOffset 列偏移量 二级列集合从0开始遍历要加上父列列索引为偏移量
     */
    private void createDataAreaOfComplexColumn(Object line, Row row, List<Column> columns, int columnOffset) {
        for (int i = 0, columnsSize = columns.size(); i < columnsSize; i++) {
            Column column = columns.get(i);
            int columnIndex = columnOffset + i;
            if (column.isComplex()) {
                Object childLine = getCellValue(line, column);
                createDataAreaOfComplexColumn(childLine, row, column.getChildColumns(), columnIndex);
            } else {
                createDataCell(line, row, columnIndex, column);
            }
        }
    }

    /**
     * 创建数据单元格
     *
     * @param line        行数据
     * @param row         Excel行对象
     * @param columnIndex 列索引
     * @param column      列对象
     */
    private void createDataCell(Object line, Row row, int columnIndex, Column column) {
        Cell cell = row.createCell(columnIndex);
        Object cellValue = getCellValue(line, column);
        setCellData(cell, column, cellValue);
        setCellStyle(cell, column);
    }

    /**
     * 设置单元格数据
     *
     * @param cell   单元格
     * @param column 列类型
     * @param value  单元格值
     */
    private void setCellData(Cell cell, Column column, Object value) {
        switch (column.getColumnType()) {
            case INT:
                if (value != null) {
                    long l;
                    if (value instanceof Number) {
                        l = ((Number) value).longValue();
                    } else if (value instanceof String) {
                        String str = (String) value;
                        if (str.isEmpty()) {
                            str = "0";
                        }
                        l = Long.parseLong(str);
                    } else {
                        throw new NumberFormatException("不支持的整数类型" + value.getClass().getSimpleName());
                    }
                    cell.setCellValue(l);
                } else {
                    cell.setCellValue(0);
                }
                break;
            case AMOUNT:
                if (value != null) {
                    BigDecimal amount;
                    if (value instanceof Number) {
                        amount = BigDecimal.valueOf(((Number) value).doubleValue());
                    } else if (value instanceof String) {
                        String str = (String) value;
                        if (str.isEmpty()) {
                            str = "0";
                        }
                        amount = new BigDecimal(str);
                    } else {
                        throw new NumberFormatException("不支持的金额类型" + value.getClass().getSimpleName());
                    }
                    cell.setCellValue(amount.setScale(2, RoundingMode.HALF_UP).toString());
                } else {
                    cell.setCellValue(0);
                }
                break;
            case DOUBLE:
                if (value != null) {
                    double d;
                    if (value instanceof Number) {
                        d = ((Number) value).doubleValue();
                    } else if (value instanceof String) {
                        String str = (String) value;
                        if (str.isEmpty()) {
                            str = "0";
                        }
                        d = Double.parseDouble(str);
                    } else {
                        throw new NumberFormatException("不支持的浮点数类型" + value.getClass().getSimpleName());
                    }
                    cell.setCellValue(d);
                } else {
                    cell.setCellValue(0d);
                }
                break;
            case TIME:
                if (value != null) {
                    Date t;
                    if (value instanceof Date) {
                        t = (Date) value;
                    } else if (value instanceof LocalDateTime) {
                        t = Date.from(((LocalDateTime) value).atZone(ZoneId.systemDefault()).toInstant());
                    } else if (value instanceof LocalDate) {
                        t = Date.from(((LocalDate) value).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
                    } else if (value instanceof Calendar) {
                        t = ((Calendar) value).getTime();
                    } else {
                        throw new RuntimeException("不支持的日期类型" + value.getClass().getSimpleName());
                    }
                    cell.setCellValue(t);
                } else {
                    cell.setCellValue("");
                }
                break;
            case STRING:
            default:
                cell.setCellValue(value == null ? "" : value.toString());
        }
    }

    /**
     * 设置头部样式
     *
     * @param tableHeader 表头
     */
    private void setTableHeaderStyle(Cell tableHeader) {
        Font font = workbook.createFont();
        font.setFontHeightInPoints(DEFAULT_HEADER_FONT_SIZE);
        font.setFontName(DEFAULT_FONT_NAME);

        CellStyle cs = workbook.createCellStyle();
        cs.setFont(font);

        cs.setAlignment(HorizontalAlignment.CENTER);
        cs.setVerticalAlignment(VerticalAlignment.CENTER);

        tableHeader.getRow().setHeightInPoints(DEFAULT_HEADER_HEIGHT);
        tableHeader.setCellStyle(cs);
    }

    /**
     * 设置列样式
     *
     * @param columnTitle 列标题
     */
    private void setColumnStyle(Cell columnTitle) {
        CellStyle cs = workbook.createCellStyle();

        Font font = workbook.createFont();
        font.setFontHeightInPoints(DEFAULT_ROW_FONT_SIZE);
        font.setFontName(DEFAULT_FONT_NAME);

        cs.setFont(font);
        cs.setAlignment(HorizontalAlignment.CENTER);
        cs.setVerticalAlignment(VerticalAlignment.CENTER);

        columnTitle.getRow().setHeightInPoints(DEFAULT_COLUMN_HEIGHT);
        columnTitle.setCellStyle(cs);
    }

    /**
     * 设置单元格样式
     *
     * @param cell   单元格
     * @param column 列类型
     */
    private void setCellStyle(Cell cell, Column column) {
        CellStyle cs = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontHeightInPoints(DEFAULT_ROW_FONT_SIZE);
        font.setFontName(DEFAULT_FONT_NAME);

        cs.setFont(font);
        cs.setAlignment(HorizontalAlignment.CENTER);
        cs.setVerticalAlignment(VerticalAlignment.CENTER);
        cell.setCellStyle(cs);

        switch (column.getColumnType()) {
            case INT:
                cs.setDataFormat(workbook.createDataFormat().getFormat("0"));
                break;
            case DOUBLE:
                cs.setDataFormat(workbook.createDataFormat().getFormat("0.00"));
                break;
            case AMOUNT:
                cs.setDataFormat(workbook.createDataFormat().getFormat("#,###.00"));
                cs.setAlignment(HorizontalAlignment.CENTER);
                break;
            case TIME:
                cs.setDataFormat(workbook.createDataFormat().getFormat("m/d/yy h:mm"));
                break;
            default:
        }
        ColumnAlign columnAlign = column.getColumnAlign();
        if (columnAlign != null) {
            cs.setAlignment(HorizontalAlignment.forInt(columnAlign.getCode()));
        }
    }


    private Object getCellValue(Object line, Column column) {
        String fieldName = column.getKey();
        if (line instanceof Map) {
            if (fieldName == null) {
                throw new IllegalArgumentException("Map数据只持者Key名称取值");
            }
            return ((Map) line).get(column.getKey());
        } else {
            Function fieldFunc = column.getKeyFunc();
            if (fieldFunc != null) {
                return fieldFunc.apply(line);
            }
            if (fieldName != null) {
                try {
                    Field field = line.getClass().getDeclaredField(fieldName);
                    boolean unaccessible = !Modifier.isPublic(field.getModifiers())
                            || !Modifier.isPublic(field.getDeclaringClass().getModifiers())
                            || Modifier.isFinal(field.getModifiers());
                    if (unaccessible && !field.isAccessible()) {
                        field.setAccessible(true);
                    }
                    return field.get(line);
                } catch (Exception e) {
                    throw new IllegalArgumentException(
                            String.format("类[%s]不包含字段[%s]", line.getClass().getName(), fieldName));
                }
            } else {
                throw new IllegalArgumentException("未设置列取值字段名或方法");
            }
        }
    }

    /**
     * 列占用的最大行数 （单列占一行，复合列没嵌套一层加一行）
     *
     * @param columns 列集合
     * @return 行数
     */
    private int maxColumnRowNum(List<Column> columns) {
        if (columns == null || columns.isEmpty()) {
            return 1;
        }
        return columns.stream()
                .map(Column::needRowNum)
                .max(Comparator.comparingInt(Integer::intValue))
                .orElse(1);
    }

    public static class Column {

        /**
         * 列标题
         */
        private String title;

        /**
         * 取值属性名
         */
        private String key;

        /**
         * 取值方法
         */
        private Function keyFunc;

        /**
         * 列宽
         */
        private int width;

        /**
         * 列类型
         */
        private ColumnType columnType;

        /**
         * 列居中类型
         */
        private ColumnAlign columnAlign;

        /**
         * 子列（多列表头）
         */
        private List<Column> childColumns;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public Function getKeyFunc() {
            return keyFunc;
        }

        public void setKeyFunc(Function keyFunc) {
            this.keyFunc = keyFunc;
        }

        public int getWidth() {
            return width;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public ColumnType getColumnType() {
            return columnType;
        }

        public void setColumnType(ColumnType columnType) {
            this.columnType = columnType;
        }

        public ColumnAlign getColumnAlign() {
            return columnAlign;
        }

        public void setColumnAlign(ColumnAlign columnAlign) {
            this.columnAlign = columnAlign;
        }

        public List<Column> getChildColumns() {
            return childColumns;
        }

        public void setChildColumns(List<Column> childColumns) {
            this.childColumns = childColumns;
        }

        public void addChildColumn(Column childColumn) {
            if (this.childColumns == null) {
                this.childColumns = new ArrayList<>();
            }
            this.childColumns.add(childColumn);
        }

        public boolean isComplex() {
            return childColumns != null && !childColumns.isEmpty();
        }

        public int needRowNum() {
            if (!isComplex()) {
                return 1;
            }
            return 1 + childColumns.stream()
                    .map(Column::needRowNum)
                    .max(Comparator.comparingInt(Integer::intValue))
                    .orElse(1);
        }


        @Override
        public String toString() {
            return "Column{" +
                    "title='" + title + '\'' +
                    ", key='" + key + '\'' +
                    ", keyFunc=" + keyFunc +
                    ", width=" + width +
                    ", columnType=" + columnType +
                    ", columnAlign=" + columnAlign +
                    ", childColumn=" + childColumns +
                    '}';
        }
    }

    public enum ColumnType {
        /**
         * 字符串
         */
        STRING,
        /**
         * 整数
         */
        INT,
        /**
         * 浮点数
         */
        DOUBLE,
        /**
         * 时间
         */
        TIME,
        /**
         * 金额(默认居右)
         */
        AMOUNT
    }

    public enum ColumnAlign {
        /* 顾名思义 */
        GENERAL,
        LEFT,
        CENTER,
        RIGHT,
        FILL,
        JUSTIFY,
        CENTER_SELECTION,
        DISTRIBUTED;

        public short getCode() {
            return (short)this.ordinal();
        }
    }
}

