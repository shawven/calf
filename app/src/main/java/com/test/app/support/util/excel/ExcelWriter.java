package com.test.app.support.util.excel;

import com.test.app.support.util.ReflectHelpers;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static java.nio.charset.StandardCharsets.ISO_8859_1;

/**
 * Excel工具
 *
 * @author Shoven
 * @date 2018-12-27
 */
public class ExcelWriter {

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
     * 数据起始行位置
     */
    private int dataRowIndexAt;

    /**
     * 列名
     */
    private List<Column> columns;

    /**
     * 表名
     */
    private String tableName;

    /**
     * 数据
     */
    private List data;

    /**
     * 扩展名
     */
    private String ext;

    /**
     * 工作薄
     */
    private Workbook workbook;

    /**
     * 工作表
     */
    private Sheet sheet;

    public ExcelWriter() {
        columns = new ArrayList<>();
        ext = "xlsx";
    }


    /**
     * 写到输出流
     *
     * @param out
     * @throws IOException
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
     * @param response
     * @throws IOException
     */
    public void writeToHttpResponse(HttpServletResponse response, String name) throws IOException {
        createSheet();

        response.reset();
        response.setContentType("application/vnd.ms-struct;charset=utf-8");
        response.setHeader("Content-Disposition", "attachment;filename=" +
                new String((name + "." + ext).getBytes(), ISO_8859_1));

        ServletOutputStream out = response.getOutputStream();
        workbook.write(out);
        out.flush();
        out.close();
    }

    /**
     * 写到文件
     *
     * @return
     * @throws IOException
     */
    public String writeToFile(String name) throws IOException {
        createSheet();

        String fileName = name + "." + ext;
        FileOutputStream out = new FileOutputStream(fileName);
        workbook.write(out);
        out.flush();
        out.close();
        return fileName;
    }

    /**
     * 创建工作表
     */
    private void createSheet() {
        workbook = new XSSFWorkbook();
        // 创建第一个sheet（页），并命名
        this.sheet = workbook.createSheet(tableName);
        // 设置表头
        setHeader();
        // 设置列标题
        setColumns();
        // 设置单元格
        setCells();
    }

    /**
     * 设置头部
     */
    private void setHeader() {
        if (tableName == null) {
            return;
        }
        Cell tableHeader = sheet.createRow(0).createCell(0);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, columns.size() - 1));
        tableHeader.setCellValue(tableName);
        setTableHeaderStyle(tableHeader);
    }

    /**
     * 设置列
     */
    private void setColumns() {
        Row columnTitleRow = sheet.createRow(1);
        for (int i = 0, columnsSize = columns.size(); i < columnsSize; i++) {
            Column column = columns.get(i);
            if (column.getField() == null) {
                continue;
            }
            // 创建列
            Cell columnTitle = columnTitleRow.createCell(i);
            columnTitle.setCellValue(column.getTitle());
            //设置列宽高
            Integer width = column.getWidth();
            sheet.setColumnWidth(i,width != null ? 256 * width : 2560);
            setColumnStyle(columnTitle);
        }
    }

    /**
     * 设置单元格
     */
    private void setCells() {
        if (data == null) {
            return;
        }
        int dataSize = data.size();
        int columnsSize = columns.size();
        countRowOffset();

        // 从数据行索引起点开始 遍历数据源数据
        for (int i = 0; i < dataSize; i++) {
            int rowIndex = getRowIndexWithOffset(i);
            Row row = sheet.createRow(rowIndex);
            Object line = data.get(i);

            for (int columnIndex = 0; columnIndex < columnsSize; columnIndex++) {
                Column column = columns.get(columnIndex);
                Object cellValue = getCellValue(line, column.getField());

                Cell cell = row.createCell(columnIndex);
                setCellData(cell, column.getColumnType(), cellValue);
                setCellStyle(cell, column.getColumnType());
            }
        }
    }

    /**
     * 计算偏移量
     */
    private void countRowOffset() {
        dataRowIndexAt += tableName != null && !tableName.isEmpty() ? 2 : 1;
    }

    /**
     * 获取贷偏移量的数据行索引
     *
     * @param i
     * @return
     */
    private int getRowIndexWithOffset(int i) {
        return i + dataRowIndexAt;
    }

    /**
     * 设置单元格数据
     *
     * @param cell
     * @param columnType
     * @param originalValue
     */
    private void setCellData(Cell cell, ColumnType columnType, Object originalValue) {
        switch (columnType) {
            case INTEGER:
                if (originalValue != null) {
                    cell.setCellValue(Integer.parseInt(originalValue.toString()));
                } else {
                    cell.setCellValue(0);
                }
                break;
            case DOUBLE:
            case AMOUNT:
                if (originalValue != null && originalValue.toString().length() > 0
                        && Character.isWhitespace(originalValue.toString().charAt(0))) {
                    cell.setCellValue(Double.parseDouble(originalValue.toString()));
                } else {
                    cell.setCellValue(0d);
                }
                break;
            case DATE:
                if (originalValue != null) {
                    cell.setCellValue((Date) originalValue);
                } else {
                    cell.setCellValue("");
                }
                break;
            case STRING:
            default:
                cell.setCellValue(originalValue == null ? "" : originalValue.toString());
        }
    }

    /**
     * 设置头部样式
     *
     * @param tableHeader
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
     * @param columnTitle
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
     * @param cell
     * @param columnType
     */
    private void setCellStyle(Cell cell, ColumnType columnType) {
        CellStyle cs = workbook.createCellStyle();
        switch (columnType) {
            case INTEGER:
                cs.setDataFormat(workbook.createDataFormat().getFormat("0"));
                break;
            case DOUBLE:
                cs.setDataFormat(workbook.createDataFormat().getFormat("0.00"));
                break;
            case AMOUNT:
                cs.setDataFormat(workbook.createDataFormat().getFormat("#,###.00"));
            case DATE:
                cs.setDataFormat(workbook.createDataFormat().getFormat("m/d/yy h:mm"));
                break;
            default:
        }

        Font font = workbook.createFont();
        font.setFontHeightInPoints(DEFAULT_ROW_FONT_SIZE);
        font.setFontName(DEFAULT_FONT_NAME);

        cs.setFont(font);
        cs.setAlignment(HorizontalAlignment.CENTER);
        cs.setVerticalAlignment(VerticalAlignment.CENTER);
        cell.setCellStyle(cs);
    }


    /**
     * 获取单元格数据
     *
     * @param line
     * @param field
     * @return
     */
    private Object getCellValue(Object line, String field) {
        if (line instanceof Map) {
            return ((Map) line).get(field);
        }
        try {
            return ReflectHelpers.getProperty(line, field);
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    String.format("类[%s]不包含字段[%s]", line.getClass().getName(), field));
        }
    }

    public ExcelWriter setColumn(String title, String field, int width, ColumnType columnType) {
        Column column = new Column();
        column.setTitle(title);
        column.setField(field);
        column.setWidth(width);
        column.setColumnType(columnType);
        this.columns.add(column);
        return this;
    }

    public ExcelWriter setColumn(String title, String field, ColumnType columnType) {
        Column column = new Column();
        column.setTitle(title);
        column.setField(field);
        column.setColumnType(columnType);
        this.columns.add(column);
        return this;
    }

    public ExcelWriter setColumn(Column column) {
        this.columns.add(column);
        return this;
    }

    public ExcelWriter setColumn(List<Column> columns) {
        this.columns.addAll(columns);
        return this;
    }

    public ExcelWriter setTableName(String tableName) {
        this.tableName = tableName;
        return this;
    }

    public ExcelWriter setData(List data) {
        this.data = data;
        return this;
    }

    public ExcelWriter setExt(String ext) {
        this.ext = ext;
        return this;
    }

    public ExcelWriter setDataRowIndexAt(int dataRowIndexAt) {
        this.dataRowIndexAt = dataRowIndexAt;
        return this;
    }

    public enum ColumnType {
        /**
         * 字符串
         */
        STRING,
        /**
         * 整数
         */
        INTEGER,
        /**
         * 小数
         */
        DOUBLE,
        /**
         * 时间
         */
        DATE,
        /**
         * 金额
         */
        AMOUNT
    }

    public static class Column {

        private String title;

        private String field;

        private Integer width;

        private ColumnType columnType;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getField() {
            return field;
        }

        public void setField(String field) {
            this.field = field;
        }

        public Integer getWidth() {
            return width;
        }

        public void setWidth(Integer width) {
            this.width = width;
        }

        public ColumnType getColumnType() {
            return columnType;
        }

        public void setColumnType(ColumnType columnType) {
            this.columnType = columnType;
        }
    }
}

